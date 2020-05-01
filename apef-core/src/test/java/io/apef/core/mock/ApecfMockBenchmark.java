package io.apef.core.mock;

import io.apef.core.APEF;
import io.apef.core.channel.ChannelConfig;
import io.apef.core.channel.MessageType;
import io.apef.core.channel.impl.FromBChannelPipeIml;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

import static io.apef.testing.benchmark.Benchmark.benchmark;

public class ApecfMockBenchmark extends BaseUnitSpec {
    //poor performance, about 50000 tps
    @Test
    public void benchmarkRequestMock() {
        benchmark()
                .concurrency(1000)
                .iterations(100)
                .rounds(10)
                .warmupConcurrency(10)
                .warmupIterations(10)
                .warmupRounds(5)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    ApecfRequestMockTest.TestRequest request = new ApecfRequestMockTest.TestRequestMessage();
                    ApecfRequestMockTest.TestRequest mockRequest = ApecfRequestMock.from(request)
                            .resultSupplier(request1 -> new ApecfRequestMockTest.TestResultMock().response(index))
                            .endMock();
                    mockRequest.onSuccess(outputValue -> {
                        runnerContext.done(index);
                    }).onFailure((errMsg, cause) -> {
                        log.error("Benchmark recept error: " + cause);
                        runnerContext.done(index);
                    }).messageType(MessageType.newType())
                            .requestContent(index);
                    mockRequest.future();
                })
                .start();
    }

    //very high performance
    @Test
    public void benchmarkChannelPipeMock() {
        ChannelPipeMock<TestChannelPipe> channelPipeMock = ChannelPipeMock.mock();
        MessageType messageType1 = MessageType.newType(0);
        TestChannelPipe mockChannelPipe = channelPipeMock.bind(new TestChannelPipe());
        ApecfMockStubber.ApecfMockStub<String, String>
                requestMock1 = channelPipeMock.mock(messageType1);
        requestMock1.filter(s -> s.equals("a"))
                .response("b").endMock()
                .filter(s -> s.equals("b"))
                .response("c").endMock();
        benchmark()
                .concurrency(500)
                .iterations(1000)
                .rounds(10)
                .warmupConcurrency(10)
                .warmupIterations(10)
                .warmupRounds(5)
                .async(false)
                .benchmarkTask((index, runnerContext) -> {
                    mockChannelPipe
                            .request()
                            .requestContent("a")
                            .onSuccess(outputValue -> {
                                runnerContext.done(index);
                            }).onFailure((errMsg, cause) -> {
                        log.error("Benchmark recept error: " + cause);
                        runnerContext.done(index);
                    }).messageType(messageType1).end();
                })
                .start();
    }

    public static class TestChannelPipe extends FromBChannelPipeIml {

        public TestChannelPipe() {
            super(APEF.createBusinessChannel(new ChannelConfig().setName("b")),
                    APEF.createBusinessChannel(new ChannelConfig().setName("c")));
        }
    }
}
