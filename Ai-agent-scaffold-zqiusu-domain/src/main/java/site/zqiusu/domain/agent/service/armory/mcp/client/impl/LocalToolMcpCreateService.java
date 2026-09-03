package site.zqiusu.domain.agent.service.armory.mcp.client.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;
import site.zqiusu.domain.agent.service.armory.mcp.client.ToolMcpCreateService;

import javax.annotation.Resource;

@Slf4j
@Service
public class LocalToolMcpCreateService implements ToolMcpCreateService {

    @Resource
    protected ApplicationContext applicationContext;

    @Override
    public ToolCallback[] buildToolCallback(AiAgentConfigTableVO.Module.ChatModel.ToolMcp toolMcp) {
        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.LocalServerParameters localConfig = toolMcp.getLocal();
        String name = localConfig.getName();

        ToolCallbackProvider localToolCallbackProvider = (ToolCallbackProvider) applicationContext.getBean(localConfig.getName());
        log.info("tool local mcp initialize {}", name);

        return localToolCallbackProvider.getToolCallbacks();
    }
}
