package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.model.enums.AppointmentStatus;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.app.mapper.ViewAppointmentMapper;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * 预约看房业务校验单元测试：时间/发布状态/重复预约
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("预约看房校验")
class ViewAppointmentServiceImplTest {

    @Mock
    private ViewAppointmentMapper viewAppointmentMapper;
    @Mock
    private ApartmentInfoService apartmentInfoService;

    @InjectMocks
    private ViewAppointmentServiceImpl service;

    private ViewAppointment appointment;
    private ApartmentInfo releasedApartment;

    /**
     * MP 3.5 的 saveOrUpdate/getById 依赖 TableInfo 元数据，
     * 纯单测（无 Spring/MyBatis 容器）需手动初始化
     */
    @BeforeAll
    static void initTableInfo() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""), ViewAppointment.class);
    }

    @BeforeEach
    void setUp() {
        appointment = new ViewAppointment();
        appointment.setUserId(100L);
        appointment.setApartmentId(10L);
        appointment.setAppointmentTime(new Date(System.currentTimeMillis() + 86400_000L)); // 明天

        releasedApartment = new ApartmentInfo();
        releasedApartment.setId(10L);
        releasedApartment.setIsRelease(ReleaseStatus.RELEASED);
    }

    private void mockApartmentOk() {
        when(apartmentInfoService.getById(10L)).thenReturn(releasedApartment);
        when(viewAppointmentMapper.selectCount(any())).thenReturn(0L);
        when(viewAppointmentMapper.insert(any())).thenReturn(1);
    }

    @Test
    @DisplayName("预约时间为过去时被拒绝")
    void save_pastTime_throws() {
        appointment.setAppointmentTime(new Date(System.currentTimeMillis() - 86400_000L));
        assertThatThrownBy(() -> service.saveOrUpdate(appointment))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("预约时间必须在未来");
    }

    @Test
    @DisplayName("预约时间缺失被拒绝")
    void save_nullTime_throws() {
        appointment.setAppointmentTime(null);
        assertThatThrownBy(() -> service.saveOrUpdate(appointment))
                .isInstanceOf(LeaseException.class);
    }

    @Test
    @DisplayName("公寓ID缺失被拒绝")
    void save_nullApartmentId_throws() {
        appointment.setApartmentId(null);
        assertThatThrownBy(() -> service.saveOrUpdate(appointment))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("公寓ID不能为空");
    }

    @Test
    @DisplayName("公寓不存在被拒绝")
    void save_apartmentNotFound_throws() {
        when(apartmentInfoService.getById(10L)).thenReturn(null);
        assertThatThrownBy(() -> service.saveOrUpdate(appointment))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("公寓不存在");
    }

    @Test
    @DisplayName("公寓未发布不可预约")
    void save_apartmentNotReleased_throws() {
        ApartmentInfo notReleased = new ApartmentInfo();
        notReleased.setId(10L);
        notReleased.setIsRelease(ReleaseStatus.NOT_RELEASED);
        when(apartmentInfoService.getById(10L)).thenReturn(notReleased);

        assertThatThrownBy(() -> service.saveOrUpdate(appointment))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("公寓未发布");
    }

    @Test
    @DisplayName("同一用户重复预约同一公寓被拒绝（防重复提交）")
    void save_duplicateAppointment_throws() {
        when(apartmentInfoService.getById(10L)).thenReturn(releasedApartment);
        when(viewAppointmentMapper.selectCount(any())).thenReturn(1L); // 已存在未取消的预约

        assertThatThrownBy(() -> service.saveOrUpdate(appointment))
                .isInstanceOf(LeaseException.class)
                .hasMessageContaining("请勿重复预约");
    }

    @Test
    @DisplayName("合法新增：默认状态为待看房并成功落库")
    void save_validNew_setsWaitingAndInserts() {
        mockApartmentOk();

        boolean result = service.saveOrUpdate(appointment);

        assertThat(result).isTrue();
        assertThat(appointment.getAppointmentStatus()).isEqualTo(AppointmentStatus.WAITING);
    }
}
