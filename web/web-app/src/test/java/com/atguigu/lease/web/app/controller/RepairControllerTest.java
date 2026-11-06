package com.atguigu.lease.web.app.controller;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.entity.RepairRequest;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.model.enums.RepairStatus;
import com.atguigu.lease.web.app.controller.repair.RepairController;
import com.atguigu.lease.web.app.service.LeaseAgreementService;
import com.atguigu.lease.web.app.service.RepairRequestService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 报修接口越权（IDOR）回归测试。
 * 历史缺陷：查询已签约租约时未按当前登录用户过滤，任何登录用户提交报修
 * 都会引用到他人租约的房间/公寓。修复后查询条件必须携带当前用户邮箱。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("报修接口IDOR越权回归")
class RepairControllerTest {

    @Mock
    private RepairRequestService repairService;
    @Mock
    private LeaseAgreementService agreementService;

    @InjectMocks
    private RepairController controller;

    private RepairRequest repair;

    @BeforeAll
    static void initTableInfo() {
        // LambdaQueryWrapper 解析实体字段需要 TableInfo 元数据
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), LeaseAgreement.class);
    }

    @BeforeEach
    void setUp() {
        repair = new RepairRequest();
        repair.setTitle("水管漏水");
        repair.setDescription("厨房水管接口漏水");
        LoginUserHolder.clear();
    }

    @AfterEach
    void tearDown() {
        LoginUserHolder.clear();
    }

    @Test
    @DisplayName("未登录提交报修被拒绝（501）")
    void save_notLogin_throws() {
        LoginUserHolder.setLoginUser(null);

        assertThatThrownBy(() -> controller.save(repair))
                .isInstanceOf(LeaseException.class)
                .extracting(e -> ((LeaseException) e).getCode())
                .isEqualTo(501);
    }

    @Test
    @DisplayName("当前用户无有效租约时报修被拒绝（不得引用他人租约）")
    void save_noOwnLease_throws() {
        LoginUserHolder.setLoginUser(new LoginUser(2L, "userB@test.com"));
        // 库中不存在 userB 的已签约租约
        when(agreementService.getOne(any())).thenReturn(null);

        assertThatThrownBy(() -> controller.save(repair))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("您没有有效的租约");
    }

    @Test
    @DisplayName("租约查询条件携带当前登录用户邮箱（归属过滤生效，防 IDOR）")
    void save_queryWrapperContainsCurrentUserEmail() {
        LoginUserHolder.setLoginUser(new LoginUser(2L, "userB@test.com"));
        when(agreementService.getOne(any())).thenReturn(null);

        try {
            controller.save(repair);
        } catch (LeaseException expected) {
            // 预期拒绝
        }

        @SuppressWarnings("unchecked")
        ArgumentCaptor<LambdaQueryWrapper<LeaseAgreement>> captor =
                ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(agreementService).getOne(captor.capture());
        // MP 3.5 的 SQL 参数为惰性求值，先触发 segment 生成再取参数
        LambdaQueryWrapper<LeaseAgreement> wrapper = captor.getValue();
        wrapper.getSqlSegment();
        Collection<Object> params = wrapper.getParamNameValuePairs().values();
        assertThat(params).contains("userB@test.com"); // 查询参数中必须包含本人邮箱
    }

    @Test
    @DisplayName("合法提交：租约房间/公寓归属当前用户且状态为待处理")
    void save_valid_fillsRoomAndApartmentFromOwnLease() {
        LoginUserHolder.setLoginUser(new LoginUser(1L, "userA@test.com"));

        LeaseAgreement ownLease = new LeaseAgreement();
        ownLease.setId(100L);
        ownLease.setEmail("userA@test.com");
        ownLease.setApartmentId(10L);
        ownLease.setRoomId(20L);
        ownLease.setStatus(LeaseStatus.SIGNED);
        when(agreementService.getOne(any())).thenReturn(ownLease);

        controller.save(repair);

        ArgumentCaptor<RepairRequest> captor = ArgumentCaptor.forClass(RepairRequest.class);
        verify(repairService).save(captor.capture());
        RepairRequest saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getApartmentId()).isEqualTo(10L);
        assertThat(saved.getRoomId()).isEqualTo(20L);
        assertThat(saved.getStatus()).isEqualTo(RepairStatus.PENDING);
    }
}
