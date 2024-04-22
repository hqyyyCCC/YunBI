package com.hqy.YunBI.manager;

import com.hqy.YunBI.common.ErrorCode;
import com.hqy.YunBI.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class AiManager {
    @Resource
    private YuCongMingClient client;

    public String doChat(long biModel,String message){
        DevChatRequest devChatRequest = new DevChatRequest();

        devChatRequest.setModelId(biModel);
        devChatRequest.setMessage(message);
        BaseResponse<DevChatResponse> response = client.doChat(devChatRequest);

        if(response==null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"AI响应错误");
        }


        return response.getData().getContent();
    }
}
