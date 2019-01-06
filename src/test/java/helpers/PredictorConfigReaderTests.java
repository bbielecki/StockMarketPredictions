package helpers;

import DomainObjects.ModelConfig;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class PredictorConfigReaderTests {
    @Test
    public void readPredictorConfigsTest(){
        //prepare
        List<ModelConfig> configs;

        //act
        configs = PredictorConfigReader.getAsList();

        //assert
        Assert.assertNotNull(configs);
        Assert.assertTrue(configs.size() > 0);
    }
}
