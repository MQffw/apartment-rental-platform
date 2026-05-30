package com.atguigu.lease.web.admin.vo.system.user;

import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.enums.SystemUserType;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
@Schema(description = "后台管理系统用户基本信息实体")
public class SystemUserItemVo extends SystemUser {

    @Schema(description = "岗位名称")
    @TableField(value = "post_name")
    private String postName;

    @Schema(description = "用户类型名称")
    @TableField(exist = false)
    private String typeName;

    public String getTypeName() {
        SystemUserType type = getType();
        return type != null ? type.getName() : null;
    }
}
