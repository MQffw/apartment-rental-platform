package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.LeaseAgreementService;
import com.atguigu.lease.web.app.vo.agreement.AgreementDetailVo;
import com.atguigu.lease.web.app.vo.agreement.AgreementItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
 * @createDate 2023-07-26 11:12:39
 */
@Service
@Slf4j
public class LeaseAgreementServiceImpl extends ServiceImpl<LeaseAgreementMapper, LeaseAgreement>
        implements LeaseAgreementService {

    @Autowired
    private LeaseAgreementMapper leaseAgreementMapper;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Override
    public List<AgreementItemVo> listItemByEmail(String email) {
        return leaseAgreementMapper.listItemByEmail(email);
    }

    @Override
    public AgreementDetailVo getDetailById(Long id, Long userId) {
        //1.查询租约信息
        LeaseAgreement leaseAgreement = leaseAgreementMapper.selectById(id);
        if (leaseAgreement == null) {
            return null;
        }
        //2.查询公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(leaseAgreement.getApartmentId());

        //3.查询房间信息
        RoomInfo roomInfo = roomInfoMapper.selectById(leaseAgreement.getRoomId());

        //4.查询图片信息
        List<GraphVo> roomGraphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, leaseAgreement.getRoomId());
        List<GraphVo> apartmentGraphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, leaseAgreement.getApartmentId());

        //5.查询支付方式
        PaymentType paymentType = paymentTypeMapper.selectById(leaseAgreement.getPaymentTypeId());

        //6.查询租期
        LeaseTerm leaseTerm = leaseTermMapper.selectById(leaseAgreement.getLeaseTermId());

        AgreementDetailVo agreementDetailVo = new AgreementDetailVo();
        BeanUtils.copyProperties(leaseAgreement, agreementDetailVo);
        agreementDetailVo.setApartmentName(apartmentInfo.getName());
        agreementDetailVo.setRoomNumber(roomInfo.getRoomNumber());
        agreementDetailVo.setApartmentGraphVoList(apartmentGraphVoList);
        agreementDetailVo.setRoomGraphVoList(roomGraphVoList);
        agreementDetailVo.setPaymentTypeName(paymentType.getName());
        agreementDetailVo.setLeaseTermMonthCount(leaseTerm.getMonthCount());
        agreementDetailVo.setLeaseTermUnit(leaseTerm.getUnit());

        return agreementDetailVo;
    }

    /**
     * \u5408\u6cd5\u7684\u72b6\u6001\u8f6c\u6362\u6620\u5c04
     * key: \u5f53\u524d\u72b6\u6001, value: \u5141\u8bb8\u8f6c\u6362\u5230\u7684\u76ee\u6807\u72b6\u6001\u96c6\u5408
     */
    private static final Map<LeaseStatus, Set<LeaseStatus>> VALID_TRANSITIONS = Map.of(
            LeaseStatus.SIGNING, Set.of(LeaseStatus.SIGNED, LeaseStatus.CANCELED),
            LeaseStatus.SIGNED, Set.of(LeaseStatus.WITHDRAWING, LeaseStatus.RENEWING),
            LeaseStatus.WITHDRAWING, Set.of(LeaseStatus.WITHDRAWN),
            LeaseStatus.RENEWING, Set.of(LeaseStatus.SIGNED)
    );

    /**
     * \u6821\u9a8c\u72b6\u6001\u8f6c\u6362\u5e76\u6267\u884cCAS\u66f4\u65b0\uff0c\u9632\u6b62\u5e76\u53d1\u4fee\u6539\u5bfc\u81f4\u72b6\u6001\u4e0d\u4e00\u81f4
     */
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
        // CAS\u66f4\u65b0\uff1aWHERE\u5e26\u4e0a\u671f\u671b\u7684\u5f53\u524d\u72b6\u6001\uff0c\u5982\u679c\u72b6\u6001\u5df2\u88ab\u5176\u4ed6\u8bf7\u6c42\u4fee\u6539\u5219affected=0
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
    public boolean saveOrUpdate(LeaseAgreement leaseAgreement) {
        return super.saveOrUpdate(leaseAgreement);
    }
}




