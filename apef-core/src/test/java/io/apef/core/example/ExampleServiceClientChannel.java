package io.apef.core.example;

import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.ServerChannelImpl;
import io.apef.core.example.impl.ExampleService;
import io.apef.core.example.interfaces.ExampleChannelPipe;

import java.util.concurrent.TimeUnit;

public class ExampleServiceClientChannel extends ServerChannelImpl {
    private ExampleChannelPipe<?, ?> exampleChannelPipe;

    public ExampleServiceClientChannel(ChannelConfig channelConfig, ExampleService exampleService) {
        super(channelConfig);
        this.exampleChannelPipe = exampleService.channelPipe(this);
    }

    private void doGet() {
        this.exampleChannelPipe
                .get().dataKey("1").idem("1").timeout(3000)
                //.requestContent(1)
                .onSuccess((responseContent) -> {
                })
                .onFailure((errMsg, cause) -> {
                })
                .end();

        //execute task in channel
        this.exampleChannelPipe.destChannel()
                .execute(() -> {
                });

        //schedule task in channel
        this.exampleChannelPipe.destChannel()
                .schedule(() -> {
                }, 1, TimeUnit.SECONDS);

        //retry task in channel
        this.exampleChannelPipe.destChannel()
                .schedule(1)
                .interval(100)
                .times(3)
                .timeUnit(TimeUnit.MILLISECONDS)
                .completeOnSuccess(true)
                .submit(input -> null)
                .onSuccess((outputValue) -> {
                })
                .onFailure((errMsg, cause) -> {
                });


        this.exampleChannelPipe.destChannel()
                .batch(1)
                .concurrency(1)
                .timeout(1)
                .timeUnit(TimeUnit.SECONDS)
                .task("task1")
                .submit(input -> null)
                .task("task2")
                .submit(input -> null)
                .start()
                .onSuccess((outputValue) -> {
                })
                .onFailure((errMsg, cause) -> {
                });
    }
}
