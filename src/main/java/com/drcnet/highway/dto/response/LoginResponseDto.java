package com.drcnet.highway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: penghao
 * @CreateTime: 2019/8/7 16:41
 * @Description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {

    private String token;

    private Integer roleId;

    private List<String> permissions;

}
