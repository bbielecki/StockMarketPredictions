package helpers;

import DomainObjects.IndexDescriptor;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IndexHistoryReader {
    //read index history starting from current - timespan up to current date
    public static List<IndexDescriptor> readHistory(LocalDate currentDate, int timeSpan, TimeUnit unit){
        throw new NotImplementedException();
    }
}
