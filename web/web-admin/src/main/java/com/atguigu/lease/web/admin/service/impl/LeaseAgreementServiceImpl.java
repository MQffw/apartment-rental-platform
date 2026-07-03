package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.atguigu.lease.web.admin.vo.agreement.AgreementQueryVo;
import com.atguigu.lease.web.admin.vo.agreement.AgreementVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liubo
 * @description 针对表【lease_agreement(租约信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
@Slf4j
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    private static final Map<LeaseStatus, Set<LeaseStatus>> VALID_TRANSITIONS = Map.of(
            LeaseStatus.SIGNING, Set.of(LeaseStatus.SIGNED, LeaseStatus.CANCELED),
            LeaseStatus.SIGNED, Set.of(LeaseStatus.WITHDRAWING, LeaseStatus.RENEWING),
            LeaseStatus.WITHDRAWING, Set.of(LeaseStatus.WITHDRAWN),
            LeaseStatus.RENEWING, Set.of(LeaseStatus.SIGNED)
    );

    public LeaseAgreementServiceImpl(LeaseAgreementMapper leaseAgreementMapper) {
        this.leaseAgreementMapper = leaseAgreementMapper;
    }

    @Override
    public AgreementVo getAgreementById(Long id) {
        AgreementVo agreementVo = leaseAgreementMapper.selectAgreementDetailById(id);
        if (agreementVo == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "租约不存在");
        }
        return agreementVo;
    }

    @Override
    public IPage<AgreementVo> pageAgreementByQuery(IPage<AgreementVo> page, AgreementQueryVo queryVo) {
        return leaseAgreementMapper.pageAgreementByQuery(page, queryVo);
    }

    @Override
    public boolean saveOrUpdate(LeaseAgreement leaseAgreement) {
        return super.saveOrUpdate(leaseAgreement);
    }

    @Override
    public void updateStatusById(Long agreementId, LeaseStatus targetStatus) {
        LeaseAgreement agreement = leaseAgreementMapper.selectById(agreementId);
        if (agreement == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "\u79df\u7ea6\u4e0d\u5b58\u5728");
        }
        LeaseStatus currentStatus = agreement.getStatus();
        Set<LeaseStatus> allowedTargets = VALID_TRANSITIONS.get(currentStatus);
        if (allowedTargets == null || !allowedTargets.contains(targetStatus)) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR,
                    String.format("\u4e0d\u5141\u8bb8\u4ece [%s] \u8f6c\u6362\u5230 [%s]", currentStatus.getName(), targetStatus.getName()));
        }
        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(LeaseAgreement::getId, agreementId);
        updateWrapper.eq(LeaseAgreement::getStatus, currentStatus);
        updateWrapper.set(LeaseAgreement::getStatus, targetStatus);
        boolean updated = leaseAgreementMapper.update(null, updateWrapper) > 0;
        if (!updated) {
            log.warn("\u79df\u7ea6\u72b6\u6001\u5e76\u53d1\u4fee\u6539\u68c0\u6d4b: id={}, \u671f\u671b\u72b6\u6001={}, \u76ee\u6807\u72b6\u6001={}",
                    agreementId, currentStatus, targetStatus);
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "\u79df\u7ea6\u72b6\u6001\u5df2\u53d8\u66f4\uff0c\u8bf7\u5237\u65b0\u540e\u91cd\u8bd5");
        }
    }

    @Override
    public List<Map<String, Object>> countLeasesByStatus() {
        return leaseAgreementMapper.countLeasesByStatus();
    }

    @Override
    public List<Map<String, Object>> countLeasesByMonth() {
        return leaseAgreementMapper.countLeasesByMonth();
    }
}




