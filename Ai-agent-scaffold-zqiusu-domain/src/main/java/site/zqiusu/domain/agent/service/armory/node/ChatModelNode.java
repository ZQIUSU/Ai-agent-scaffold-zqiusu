package site.zqiusu.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.json.jackson.JacksonMcpJsonMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;
import site.zqiusu.domain.agent.model.entity.ArmoryCommandEntity;
import site.zqiusu.domain.agent.model.valobj.AiAgentConfigTableVO;
import site.zqiusu.domain.agent.model.valobj.AiAgentRegisterVO;
import site.zqiusu.domain.agent.service.armory.AbstractArmorySupport;
import site.zqiusu.domain.agent.service.armory.factory.DefaultArmoryFactory;

import java.time.Duration;

@Slf4j
@Service
public class ChatModelNode extends AbstractArmorySupport {

    @Resource
    private AgentNode agentNode;

    @Override
    protected AiAgentRegisterVO doApply(ArmoryCommandEntity requestParameter, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        log.info("Ai Agent装配 - ChatModelNode");
        OpenAiApi openAiApi = dynamicContext.getOpenAiApi();

        AiAgentConfigTableVO aiAgentConfigTableVO = requestParameter.getAiAgentConfigTableVO();
        AiAgentConfigTableVO.Module.ChatModel chatModelConfig = aiAgentConfigTableVO.getModule().getChatModel();

        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(aiAgentConfigTableVO.getModule().getChatModel().getModel())
                        .toolCallbacks(SyncMcpToolCallbackProvider.builder()
                                .mcpClients(githubMcpClient(aiAgentConfigTableVO.getModule().getChatModel())).build()
                                .getToolCallbacks())
                        .build())
                .build();

        dynamicContext.setChatModel(chatModel);

        String call = chatModel.call("你哪有哪些工具能力");

        log.info("测试结果:{}", call);


        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryFactory.DynamicContext, AiAgentRegisterVO> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryFactory.DynamicContext dynamicContext) throws Exception {
        return agentNode;
    }

    private static McpSyncClient githubMcpClient(AiAgentConfigTableVO.Module.ChatModel chatModelConfig) {
        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.StdioServerParameters stdio = chatModelConfig.getToolMcpList()
                .stream()
                .map(AiAgentConfigTableVO.Module.ChatModel.ToolMcp::getStdio)
                .filter(item -> item != null && "github".equals(item.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing github stdio MCP config in test-agent.yml"));

        AiAgentConfigTableVO.Module.ChatModel.ToolMcp.StdioServerParameters.ServerParameters config = stdio.getServerParameters();

        ServerParameters serverParameters = ServerParameters.builder(config.getCommand())
                .args(config.getArgs())
                .env(config.getEnv())
                .build();

        StdioClientTransport transport = new StdioClientTransport(
                serverParameters,
                new JacksonMcpJsonMapper(new ObjectMapper()));

        McpSyncClient mcpSyncClient = McpClient.sync(transport).requestTimeout(Duration.ofMillis(stdio.getRequestTimeout())).build();
        var init = mcpSyncClient.initialize();
        log.info("GitHub MCP Initialized {}", init);

        return mcpSyncClient;
    }
}
