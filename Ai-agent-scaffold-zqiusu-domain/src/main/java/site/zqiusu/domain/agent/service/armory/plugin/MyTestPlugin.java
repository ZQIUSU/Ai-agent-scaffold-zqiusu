package site.zqiusu.domain.agent.service.armory.plugin;

import com.google.adk.agents.InvocationContext;
import com.google.adk.plugins.BasePlugin;
import com.google.genai.types.Content;
import io.reactivex.rxjava3.core.Maybe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



@Slf4j
@Service("myTestPlugin")
public class MyTestPlugin extends BasePlugin {
    public MyTestPlugin(String name) {
        super(name);
    }

    public MyTestPlugin() {
        super("MyTestPlugin");
    }

    @Override
    public Maybe<Content> onUserMessageCallback(InvocationContext invocationContext, Content userMessage) {
        String text = userMessage.text();
        log.info("用户输入信息:{}",text);
        return super.onUserMessageCallback(invocationContext, userMessage);
    }
}
