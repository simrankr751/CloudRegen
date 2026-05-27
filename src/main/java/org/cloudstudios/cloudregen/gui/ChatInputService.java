package org.cloudstudios.cloudregen.gui;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class ChatInputService {
    private final Map<UUID, InputRequest> requests = new ConcurrentHashMap<>();

    public void request(UUID playerId, long timeoutAtMillis, Consumer<String> consumer) {
        requests.put(playerId, new InputRequest(timeoutAtMillis, consumer));
    }

    public boolean consume(UUID playerId, String message) {
        InputRequest request = requests.remove(playerId);
        if (request == null) {
            return false;
        }
        if (System.currentTimeMillis() > request.timeoutAtMillis) {
            return true;
        }
        request.consumer.accept(message);
        return true;
    }

    public boolean hasPending(UUID playerId) {
        return requests.containsKey(playerId);
    }

    private record InputRequest(long timeoutAtMillis, Consumer<String> consumer) {
    }
}
