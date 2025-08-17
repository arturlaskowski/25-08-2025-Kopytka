package pl.kopytka.common.command;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@SuppressWarnings({"rawtypes", "unchecked"})
class SynchronousCommandHandlerExecutor implements CommandHandlerExecutor {

    private final Map<Class<? extends Command>, CommandHandler> handlerMap;

    public SynchronousCommandHandlerExecutor(List<CommandHandler> commandHandlers) {
        if (commandHandlers == null || commandHandlers.isEmpty()) {
            log.warn("Command handlers list is null or empty.");
            this.handlerMap = new HashMap<>();
        } else {
            this.handlerMap = commandHandlers.stream()
                    .collect(Collectors.toMap(CommandHandler::handlingCommandClass, Function.identity()));
        }
    }

    @Override
    public void execute(Command command) {
        var handler = handlerMap.get(command.getClass());
        if (handler == null) {
            throw new IllegalStateException("No command handler registered for " + command.getClass().getName());
        }
        handler.handle(command);
    }
}


