package ltd.nb6.mgmcExample;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventContext;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * 示例附属节点注册
 */
public class ExampleNodes {
    /**
     * 定义一个自定义事件类型
     * 附属可以通过 EventDispatcher.dispatch(EXAMPLE_EVENT, ...) 来触发关联的蓝图
     */
    public static final MGMCEventType EXAMPLE_EVENT = new MGMCEventType("mgmc_example:custom_trigger");

    public static void register() {
        // 1. 自定义动作节点：发送带前缀的消息
        // 该节点接收一个字符串输入，并将其发送给当前触发蓝图的玩家
        NodeHelper.setup("example_say_hello", "node.mgmc_example.say_hello.name")
            .category("node_category.mgmc.action")
            .color(0x3498db) // 蓝色，通常用于动作节点
            .execIn()
            .execOut()
            .input(NodePorts.MESSAGE, "node.mgmc.port.message", PortType.STRING, NodeThemes.COLOR_PORT_STRING, "Hello from Example Addon!")
            .registerExec((node, ctx) -> {
                // 从输入端口获取消息内容
                String message = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.MESSAGE, ctx));
                
                // 检查触发蓝图的实体是否为玩家
                if (ctx.triggerEntity instanceof ServerPlayer player) {
                    // 发送系统消息
                    player.sendSystemMessage(Component.literal("§b[Example] §f" + message));
                }
                
                // 触发执行流的输出，继续运行蓝图中连接的下一个节点
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // 2. 自定义计算节点：计算两个数的平均值
        // 这是一个纯数据节点（没有执行流），用于演示如何处理数值运算
        NodeHelper.setup("example_average", "node.mgmc_example.average.name")
            .category("node_category.mgmc.math")
            .color(0x2ecc71) // 绿色，通常用于数学或逻辑节点
            .input("val1", "node.mgmc_example.port.val1", PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input("val2", "node.mgmc_example.port.val2", PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.RESULT, "node.mgmc.port.output", PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> {
                // 获取两个输入端口的数值
                double v1 = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "val1", ctx));
                double v2 = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "val2", ctx));
                
                // 返回计算结果，蓝图引擎会自动将其提供给输出端口
                return (v1 + v2) / 2.0;
            });

        // 3. 自定义事件节点：当自定义事件触发时执行
        // 该节点作为蓝图的起点，当 EXAMPLE_EVENT 被触发时开始运行
        NodeHelper.setup("on_example_trigger", "node.mgmc_example.on_example_trigger.name")
            .category("node_category.mgmc.event")
            .color(0xe67e22) // 橙色，通常用于事件节点
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .output("data", "node.mgmc-example.port.data", PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerEvent(
                EXAMPLE_EVENT,
                (eventCtx, nodeCtxBuilder) -> {
                    // 填充蓝图执行上下文
                    // 这里可以将事件中的原始数据（如触发者、坐标）传递给蓝图引擎
                    nodeCtxBuilder.triggerEntity(eventCtx.getEntity());
                },
                eventCtx -> BlueprintRouter.GLOBAL_ID, // 路由 ID，用于区分不同的触发源（本例中使用全局 ID）
                (node, portId, ctx) -> {
                    // 提供节点输出端口的数据逻辑
                    if ("data".equals(portId)) {
                        return "Example Event Data: " + System.currentTimeMillis();
                    }
                    return null;
                }
            );
    }
}
