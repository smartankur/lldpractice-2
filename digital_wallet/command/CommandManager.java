package digital_wallet.command;

import lombok.AllArgsConstructor;
import lombok.NonNull;

import java.util.Map;

@AllArgsConstructor
public class CommandManager {
    private Map<String, ICommandExecutor> commandExecutors;

    public void executeCommand(@NonNull final String command) {
        final String[] commandParts = command.split(" ");
        final String commandName = commandParts[0];
        final ICommandExecutor commandExecutor = commandExecutors
                .get(commandName);
        commandExecutor.execute(commandParts);
    }
}
