package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.admin.vo.apartment.ApartmentSubmitVo;
import com.atguigu.lease.web.admin.vo.fee.FeeValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
@Slf4j
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private FeeValueMapper feeValueMapper;

    @Autowired
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private ApartmentFacilityService apartmentFacilityService;

    @Autowired
    private ApartmentLabelService apartmentLabelService;

    @Autowired
    private ApartmentFeeValueService apartmentFeeValueService;


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdateApartment(ApartmentSubmitVo apartmentSubmitVo) {
        boolean isupdate = apartmentSubmitVo.getId()!=null;
        super.saveOrUpdate(apartmentSubmitVo);
        //先删除再添加需要保留的图片，属性值，标签，杂费值
        if(isupdate){
            //删除图片,杂费值，属性值，标签
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
            graphQueryWrapper.eq(GraphInfo::getItemId, apartmentSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);

            LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(ApartmentFacility::getApartmentId, apartmentSubmitVo.getId());
            apartmentFacilityService.remove(facilityQueryWrapper);

            LambdaQueryWrapper<ApartmentLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
            labelQueryWrapper.eq(ApartmentLabel::getApartmentId, apartmentSubmitVo.getId());
            apartmentLabelService.remove(labelQueryWrapper);

            LambdaQueryWrapper<ApartmentFeeValue> feeValueQueryWrapper = new LambdaQueryWrapper<>();
            feeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, apartmentSubmitVo.getId());
            apartmentFeeValueService.remove(feeValueQueryWrapper);
        }
            //添加图片,杂费值，属性值，标签
            List<GraphVo> graphVoList = apartmentSubmitVo.getGraphVoList();
            if(!CollectionUtils.isEmpty(graphVoList)){
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.APARTMENT);
                graphInfo.setItemId(apartmentSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }
            List<Long> facilityInfoIdList = apartmentSubmitVo.getFacilityInfoIds();
            if(!CollectionUtils.isEmpty(facilityInfoIdList)){
            ArrayList<ApartmentFacility> facilityList = new ArrayList<>();
            for (Long facilityId : facilityInfoIdList) {
                ApartmentFacility apartmentFacility = new ApartmentFacility();
                apartmentFacility.setApartmentId(apartmentSubmitVo.getId());
                apartmentFacility.setFacilityId(facilityId);
                facilityList.add(apartmentFacility);
            }
            apartmentFacilityService.saveBatch(facilityList);
            }
            List<Long> labelIds = apartmentSubmitVo.getLabelIds();
            if (!CollectionUtils.isEmpty(labelIds)) {
                List<ApartmentLabel> apartmentLabelList = new ArrayList<>();
                for (Long labelId : labelIds) {
                    ApartmentLabel apartmentLabel = new ApartmentLabel();
                    apartmentLabel.setApartmentId(apartmentSubmitVo.getId());
                    apartmentLabel.setLabelId(labelId);
                    apartmentLabelList.add(apartmentLabel);
                }
                apartmentLabelService.saveBatch(apartmentLabelList);
            }
            List<Long> feeValueIds = apartmentSubmitVo.getFeeValueIds();
            if (!CollectionUtils.isEmpty(feeValueIds)) {
                ArrayList<ApartmentFeeValue> apartmentFeeValueList = new ArrayList<>();
                for (Long feeValueId : feeValueIds) {
                    ApartmentFeeValue apartmentFeeValue = new ApartmentFeeValue();
                    apartmentFeeValue.setApartmentId(apartmentSubmitVo.getId());
                    apartmentFeeValue.setFeeValueId(feeValueId);
                    apartmentFeeValueList.add(apartmentFeeValue);
                }
                apartmentFeeValueService.saveBatch(apartmentFeeValueList);
            }

    }

    @Override
    public IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo) {
        return apartmentInfoMapper.pageItem(page, queryVo);
    }

    @Override
    public ApartmentDetailVo getDetailById(Long id) {
        //查询公寓信息
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);
        if (apartmentInfo == null) {
            throw new LeaseException(ResultCodeEnum.DATA_ERROR, "公寓不存在");
        }
        //查询其他信息
        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT,id);

        List<LabelInfo> labelInfoList =labelInfoMapper.selectListByApartmentId(id);

        List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByApartmentId(id);

        List<FeeValueVo> feeValueVoList = feeValueMapper.selectListByApartmentId(id);
        //组装结果
        ApartmentDetailVo apartmentDetailVo = new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo,apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setFeeValueVoList(feeValueVoList);

        return apartmentDetailVo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeApartmentById(Long id) {
        LambdaQueryWrapper<RoomInfo> roomQueryWrapper = new LambdaQueryWrapper<>();
        roomQueryWrapper.eq(RoomInfo::getApartmentId, id);
        Long count = roomInfoMapper.selectCount(roomQueryWrapper);

        if (count > 0){
            //终止
            throw new LeaseException(ResultCodeEnum.DELETE_ERROR.getCode(),ResultCodeEnum.DELETE_ERROR.getMessage());
        }

        super.removeById(id);

        LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
        graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.APARTMENT);
        graphQueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphQueryWrapper);

        LambdaQueryWrapper<ApartmentFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
        facilityQueryWrapper.eq(ApartmentFacility::getApartmentId, id);
        apartmentFacilityService.remove(facilityQueryWrapper);

        LambdaQueryWrapper<ApartmentLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
        labelQueryWrapper.eq(ApartmentLabel::getApartmentId, id);
        apartmentLabelService.remove(labelQueryWrapper);

        LambdaQueryWrapper<ApartmentFeeValue> feeValueQueryWrapper = new LambdaQueryWrapper<>();
        feeValueQueryWrapper.eq(ApartmentFeeValue::getApartmentId, id);
        apartmentFeeValueService.remove(feeValueQueryWrapper);
    }
}




