package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.entity.LeaseTerm;
import com.atguigu.lease.model.entity.PaymentType;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.app.mapper.ApartmentInfoMapper;
import com.atguigu.lease.web.app.mapper.GraphInfoMapper;
import com.atguigu.lease.web.app.mapper.LeaseAgreementMapper;
import com.atguigu.lease.web.app.mapper.LeaseTermMapper;
import com.atguigu.lease.web.app.mapper.PaymentTypeMapper;
import com.atguigu.lease.web.app.mapper.RoomInfoMapper;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.mq.NotificationMessageProducer;
import com.atguigu.lease.web.app.vo.agreement.AgreementDetailVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 租约状态机 + CAS 并发安全 + saveOrUpdate 安全校验单元测试（纯 Mockito，不起容器）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("租约状态机与CAS更新")
class LeaseAgreementServiceImplTest {

    @Mock
    private LeaseAgreementMapper leaseAgreementMapper;
    @Mock
    private UserInfoMapper userInfoMapper;
    @Mock
    private ApartmentInfoMapper apartmentInfoMapper;
    @Mock
    private RoomInfoMapper roomInfoMapper;
    @Mock
    private GraphInfoMapper graphInfoMapper;
    @Mock
    private PaymentTypeMapper paymentTypeMapper;
    @Mock
    private LeaseTermMapper leaseTermMapper;
    @Mock
    private NotificationMessageProducer notificationProducer;

    @InjectMocks
    private LeaseAgreementServiceImpl service;

    private LeaseAgreement agreement;

    /**
     * LambdaUpdateWrapper 解析实体字段依赖 TableInfo 元数据（MP 全局静态缓存），
     * 单独运行本测试类时必须自行注册，避免依赖其他测试类的副作用
     */
    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), LeaseAgreement.class);
    }

    @BeforeEach
    void setUp() {
        agreement = new LeaseAgreement();
        agreement.setId(1L);
        agreement.setEmail("tenant@test.com");
        agreement.setStatus(LeaseStatus.SIGNING);
    }

    @Test
    @DisplayName("合法状态转换：SIGNING -> SIGNED 更新成功")
    void updateStatus_validTransition_success() {
        when(leaseAgreementMapper.selectById(1L)).thenReturn(agreement);
        when(leaseAgreementMapper.update(any(), any())).thenReturn(1);
        // 无匹配用户时不发通知
        when(userInfoMapper.selectOne(any())).thenReturn(null);

        service.updateStatusById(1L, LeaseStatus.SIGNED);

        verify(leaseAgreementMapper).update(any(), any());
        verify(notificationProducer, never()).sendNotification(anyLong(), any(), any(), any(), anyLong());
    }

    @Test
    @DisplayName("非法状态转换：SIGNING -> WITHDRAWN 被拒绝")
    void updateStatus_illegalTransition_throws() {
        when(leaseAgreementMapper.selectById(1L)).thenReturn(agreement);

        assertThatThrownBy(() -> service.updateStatusById(1L, LeaseStatus.WITHDRAWN))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("不允许");
    }

    @Test
    @DisplayName("CAS 竞争：状态已被并发修改（影响行数=0）时抛状态变更异常")
    void updateStatus_casConflict_throws() {
        when(leaseAgreementMapper.selectById(1L)).thenReturn(agreement);
        // 模拟并发：期望状态已变，update 影响 0 行
        when(leaseAgreementMapper.update(any(), any())).thenReturn(0);

        assertThatThrownBy(() -> service.updateStatusById(1L, LeaseStatus.SIGNED))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("状态已变更");
    }

    @Test
    @DisplayName("租约不存在时抛数据异常")
    void updateStatus_agreementNotFound_throws() {
        when(leaseAgreementMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.updateStatusById(999L, LeaseStatus.SIGNED))
                .isInstanceOf(LeaseException.class)
                .extracting(e -> ((LeaseException) e).getCode())
                .isEqualTo(ResultCodeEnum.DATA_ERROR.getCode());
    }

    @Test
    @DisplayName("状态更新成功后向租约承租人发送通知")
    void updateStatus_success_sendsNotification() {
        when(leaseAgreementMapper.selectById(1L)).thenReturn(agreement);
        when(leaseAgreementMapper.update(any(), any())).thenReturn(1);
        com.atguigu.lease.model.entity.UserInfo user = new com.atguigu.lease.model.entity.UserInfo();
        user.setId(88L);
        user.setEmail("tenant@test.com");
        when(userInfoMapper.selectOne(any())).thenReturn(user);

        service.updateStatusById(1L, LeaseStatus.SIGNED);

        ArgumentCaptor<Long> userIdCaptor = ArgumentCaptor.forClass(Long.class);
        verify(notificationProducer).sendNotification(userIdCaptor.capture(), any(), any(), any(), eq(1L));
        assertThat(userIdCaptor.getValue()).isEqualTo(88L);
    }

    @Test
    @DisplayName("getDetailById 组装公寓/房间/租期/支付方式聚合详情")
    void getDetailById_assemblesVo() {
        LeaseAgreement full = new LeaseAgreement();
        full.setId(1L);
        full.setApartmentId(10L);
        full.setRoomId(20L);
        full.setPaymentTypeId(3L);
        full.setLeaseTermId(2L);
        full.setRent(new java.math.BigDecimal("3000"));
        when(leaseAgreementMapper.selectById(1L)).thenReturn(full);

        ApartmentInfo apartment = new ApartmentInfo();
        apartment.setId(10L);
        apartment.setName("硅谷好寓");
        when(apartmentInfoMapper.selectById(10L)).thenReturn(apartment);

        RoomInfo room = new RoomInfo();
        room.setId(20L);
        room.setRoomNumber("A101");
        when(roomInfoMapper.selectById(20L)).thenReturn(room);

        List<GraphVo> graphs = Collections.emptyList();
        lenient().when(graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, 20L)).thenReturn(graphs);
        lenient().when(graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, 10L)).thenReturn(graphs);

        PaymentType paymentType = new PaymentType();
        paymentType.setId(3L);
        paymentType.setName("半年付");
        when(paymentTypeMapper.selectById(anyLong())).thenReturn(paymentType);

        LeaseTerm leaseTerm = new LeaseTerm();
        leaseTerm.setId(2L);
        leaseTerm.setMonthCount(6);
        leaseTerm.setUnit("月");
        when(leaseTermMapper.selectById(anyLong())).thenReturn(leaseTerm);

        AgreementDetailVo vo = service.getDetailById(1L, 100L);

        assertThat(vo).isNotNull();
        assertThat(vo.getApartmentName()).isEqualTo("硅谷好寓");
        assertThat(vo.getRoomNumber()).isEqualTo("A101");
        assertThat(vo.getPaymentTypeName()).isEqualTo("半年付");
        assertThat(vo.getLeaseTermMonthCount()).isEqualTo(6);
    }

    @Test
    @DisplayName("getDetailById 租约不存在返回 null")
    void getDetailById_notFound_returnsNull() {
        when(leaseAgreementMapper.selectById(999L)).thenReturn(null);
        assertThat(service.getDetailById(999L, 100L)).isNull();
    }

    @Test
    @DisplayName("listItemByEmail 直接透传 Mapper 查询")
    void listItemByEmail_delegatesToMapper() {
        service.listItemByEmail("tenant@test.com");
        verify(leaseAgreementMapper).listItemByEmail("tenant@test.com");
    }

    // ==================== saveOrUpdate 安全校验（防 IDOR / 绕过状态机） ====================

    @Test
    @DisplayName("saveOrUpdate 更新他人租约被拒绝（防 IDOR 窃取）")
    void saveOrUpdate_othersLease_throws() {
        // entity.email 由 Controller 填充为当前登录用户；库里租约属于别人
        LeaseAgreement mine = new LeaseAgreement();
        mine.setId(50L);
        mine.setEmail("attacker@test.com");
        LeaseAgreement others = new LeaseAgreement();
        others.setId(50L);
        others.setEmail("victim@test.com");
        when(leaseAgreementMapper.selectById(50L)).thenReturn(others);

        assertThatThrownBy(() -> service.saveOrUpdate(mine))
                .isInstanceOf(LeaseException.class)
                .extracting(e -> ((LeaseException) e).getCode())
                .isEqualTo(ResultCodeEnum.ILLEGAL_REQUEST.getCode());
    }

    @Test
    @DisplayName("saveOrUpdate 指定不存在的 id 被拒绝（禁止任意主键 upsert）")
    void saveOrUpdate_nonexistentId_throws() {
        LeaseAgreement mine = new LeaseAgreement();
        mine.setId(999L);
        mine.setEmail("attacker@test.com");
        when(leaseAgreementMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> service.saveOrUpdate(mine))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("租约不存在");
    }

    @Test
    @DisplayName("saveOrUpdate 状态变更走状态机校验（防绕过）")
    void saveOrUpdate_illegalStatusTransition_throws() {
        LeaseAgreement mine = new LeaseAgreement();
        mine.setId(60L);
        mine.setEmail("tenant@test.com");
        mine.setStatus(LeaseStatus.EXPIRED); // 试图 SIGNED -> EXPIRED（非法）
        LeaseAgreement existing = new LeaseAgreement();
        existing.setId(60L);
        existing.setEmail("tenant@test.com");
        existing.setStatus(LeaseStatus.SIGNED);
        when(leaseAgreementMapper.selectById(60L)).thenReturn(existing);

        assertThatThrownBy(() -> service.saveOrUpdate(mine))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("不允许");
    }
}
