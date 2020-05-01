package io.apef.core.example.interfaces;

import io.apef.core.channel.BusinessChannel;

public interface ExampleChannel<C extends ExampleChannel<C>> extends BusinessChannel<C> {
}
