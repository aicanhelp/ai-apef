package io.apef.repository;

import io.apef.core.APEF;
import io.apef.core.channel.BusinessChannel;
import io.apef.core.channel.ChannelConfig;
import io.apef.testing.unit.BaseUnitSpec;
import io.apef.repository.channel.RepositoryChannelPipe;
import org.testng.annotations.Test;

@Test
public class MockRepositoryTest extends BaseUnitSpec {
    private MockRepository<String, String> mockRepository = new MockRepository<>();
    private BusinessChannel<?> businessChannel = APEF.createBusinessChannel(new ChannelConfig().setName("buss"));
    private RepositoryChannelPipe<String, String> channelPipe
            = mockRepository.repositoryChannelPipe(businessChannel);

    public void testMockGet() {
        mockRepository.mockGet()
                .filter(o -> o.equals("a"))
                .response("b")
                .endMock();
        Blocker blocker = new Blocker();
        channelPipe.get()
                .key("a")
                .onSuccess(outputValue -> {
                    blocker.assertEquals(outputValue, "b").end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker.failAndEnd("should be success");
                })
                .end();

        blocker.awaitEnd();
    }

    public void testMockSave() {
        mockRepository.mockSave()
                .filter(o -> o.equals("a"))
                .response(true)
                .endMock();
        Blocker blocker = new Blocker();
        channelPipe.save()
                .value("a")
                .onSuccess(outputValue -> {
                    blocker.assertTrue(outputValue).end();
                })
                .onFailure((errMsg, cause) -> {
                    blocker.failAndEnd("should be success");
                })
                .end();

        blocker.awaitEnd();
    }


}