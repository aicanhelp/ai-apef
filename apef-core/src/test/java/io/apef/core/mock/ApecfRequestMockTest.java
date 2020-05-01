package io.apef.core.mock;

import io.apef.core.channel.MessageType;
import io.apef.core.channel.impl.ChannelMessageImpl;
import io.apef.core.channel.request.ChannelTxRequest;
import io.apef.testing.unit.BaseUnitSpec;
import org.testng.annotations.Test;

@Test
public class ApecfRequestMockTest extends BaseUnitSpec {

    public void testMockEndCorrect() {
        TestRequest request = new TestRequestMessage();
        TestRequest mockRequest = ApecfRequestMock.from(request)
                .resultSupplier(request1 -> new TestResultMock().response(1))
                .endMock();

        Blocker blocker = new Blocker();
        mockRequest.onSuccess(outputValue -> {
            blocker.assertEquals(outputValue, 1).end();
        }).onFailure((errMsg, cause) -> {
            blocker.failAndEnd("Should be success, errMsg: " + errMsg);
        }).messageType(MessageType.newType())
                .requestContent(1)
                .end();
        blocker.awaitEnd();
    }

    public void testMockFutureCorrect() {
        TestRequest request = new TestRequestMessage();
        TestRequest mockRequest = ApecfRequestMock.from(request)
                .resultSupplier(request1 -> new TestResultMock().response(1))
                .endMock();

        Blocker blocker = new Blocker();
        mockRequest.onSuccess(outputValue -> {
            blocker.assertEquals(outputValue, 1).end();
        }).onFailure((errMsg, cause) -> {
            blocker.failAndEnd("Should be success, errMsg: " + errMsg);
        }).messageType(MessageType.newType())
                .requestContent(1);
        mockRequest.future();
        blocker.awaitEnd();
    }

    public void testMockEndWithException() {
        TestRequest request = new TestRequestMessage();
        TestRequest mockRequest = ApecfRequestMock.from(request)
                .resultSupplier(request1 -> new TestResultMock().exception(new Exception("a")))
                .endMock();

        Blocker blocker = new Blocker();
        mockRequest.onSuccess(outputValue -> {
            blocker.failAndEnd("Should be failure");
        }).onFailure((errMsg, cause) -> {
            blocker.assertEquals(cause.getMessage(), "a").end();
        }).messageType(MessageType.newType())
                .requestContent(1)
                .end();
        blocker.awaitEnd();
    }

    public void testMockFutureWithException() {
        TestRequest request = new TestRequestMessage();
        TestRequest mockRequest = ApecfRequestMock.from(request)
                .resultSupplier(request1 -> new TestResultMock().exception(new Exception("a")))
                .endMock();

        Blocker blocker = new Blocker();
        mockRequest.onSuccess(outputValue -> {
            blocker.failAndEnd("Should be failure");
        }).onFailure((errMsg, cause) -> {
            blocker.assertEquals(cause.getMessage(), "a").end();
        }).messageType(MessageType.newType())
                .requestContent(1);
        mockRequest.future();
        blocker.awaitEnd();
    }

    public interface TestRequest extends ChannelTxRequest {

    }


    public static class TestResultMock extends ApecfRequestMock.ResultMock<Object,TestResultMock> {

    }

    public static class TestRequestMessage extends ChannelMessageImpl
            implements TestRequest {

    }
}