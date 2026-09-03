package site.zqiusu.domain.agent.service.armory.mcp.client.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;
import site.zqiusu.domain.agent.service.armory.mcp.client.ToolMcpCreateService;
import site.zqiusu.domain.agent.service.armory.mcp.client.impl.LocalToolMcpCreateService;
import site.zqiusu.domain.agent.service.armory.mcp.client.impl.SSEToolMcpCreateService;
import site.zqiusu.domain.agent.service.armory.mcp.client.impl.StdioToolMcpCreateService;
import site.zqiusu.types.enums.ResponseCode;
import site.zqiusu.types.exception.AppException;

import javax.annotation.Resource;

@Slf4j
@Service
public class DefaultMcpClientFactory {

    @Resource
    private LocalToolMcpCreateService localToolMcpCreateService;
    @Resource
    private SSEToolMcpCreateService sseToolMcpCreateService;
    @Resource
    private StdioToolMcpCreateService stdioToolMcpCreateService;

    public ToolMcpCreateService getToolMcpCreateService(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp){
        if (null != toolMcp.getLocal()) return localToolMcpCreateService;
        if (null != toolMcp.getSse()) return sseToolMcpCreateService;
        if (null != toolMcp.getStdio()) return stdioToolMcpCreateService;
        throw new AppException(ResponseCode.NOT_FOUND_METHOD.getCode(), ResponseCode.NOT_FOUND_METHOD.getInfo());

    }
}
