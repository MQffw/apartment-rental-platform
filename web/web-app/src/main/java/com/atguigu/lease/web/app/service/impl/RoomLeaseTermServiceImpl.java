package com.atguigu.lease.web.app.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.lease.model.entity.RoomLeaseTerm;
import com.atguigu.lease.web.app.service.RoomLeaseTermService;
import com.atguigu.lease.web.app.mapper.RoomLeaseTermMapper;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
* @author liubo
* @description 针对表【room_lease_term(房间租期管理表)】的数据库操作Service实现
* @createDate 2023-07-26 11:12:39
*/
@Service
@Slf4j
public class RoomLeaseTermServiceImpl extends ServiceImpl<RoomLeaseTermMapper, RoomLeaseTerm>
    implements RoomLeaseTermService{

}




