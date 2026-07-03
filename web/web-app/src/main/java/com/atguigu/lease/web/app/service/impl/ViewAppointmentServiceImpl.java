package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.model.enums.AppointmentStatus;
import com.atguigu.lease.model.enums.ReleaseStatus;
import com.atguigu.lease.web.app.mapper.ViewAppointmentMapper;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.ViewAppointmentService;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {

    @Autowired
    private ViewAppointmentMapper viewAppointmentMapper;

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Override
    public List<AppointmentItemVo> listItemByUserId(Long userId) {
        return viewAppointmentMapper.listItemByUserId(userId);
    }

    @Override
    public AppointmentDetailVo getDetailById(Long id, Long userId) {
        return viewAppointmentMapper.getDetailById(id, userId);
    }

    @Override
    public boolean saveOrUpdate(ViewAppointment entity) {
        // 1. 校验预约时间是否在未来
        if (entity.getAppointmentTime() == null || !entity.getAppointmentTime().after(new Date())) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "预约时间必须在未来");
        }

        // 2. 校验公寓是否存在且已发布
        if (entity.getApartmentId() == null) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR, "公寓ID不能为空");
        }
        ApartmentInfo apartment = apartmentInfoService.getById(entity.getApartmentId());
        if (apartment == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "公寓不存在");
        }
        if (apartment.getIsRelease() != ReleaseStatus.RELEASED) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "公寓未发布，暂不可预约");
        }

        // 3. 校验是否重复预约（同一用户、同一公寓、未取消的预约）
        if (entity.getUserId() != null) {
            LambdaQueryWrapper<ViewAppointment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ViewAppointment::getUserId, entity.getUserId());
            queryWrapper.eq(ViewAppointment::getApartmentId, entity.getApartmentId());
            queryWrapper.ne(ViewAppointment::getAppointmentStatus, AppointmentStatus.CANCELED);
            // 如果是更新操作，排除自身
            if (entity.getId() != null) {
                queryWrapper.ne(ViewAppointment::getId, entity.getId());
            }
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new LeaseException(ResultCodeEnum.REPEAT_SUBMIT, "您已预约过该公寓，请勿重复预约");
            }
        }

        // 4. 新增时默认状态为待看房
        if (entity.getId() == null) {
            entity.setAppointmentStatus(AppointmentStatus.WAITING);
        }

        return super.saveOrUpdate(entity);
    }
}
