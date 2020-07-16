package fi.aalto.cs.apluscourses.utils.observable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import org.junit.Assert;
import org.junit.Test;

public class CompoundObservablePropertyTest {

  @Test
  public void testCompoundObservableProperty() {
    AtomicInteger converterCallCount = new AtomicInteger(0);
    BinaryOperator<Integer> converter = (a, b) -> {
      converterCallCount.incrementAndGet();
      return a / b;
    };

    ObservableProperty<Integer> property1 = new ObservableReadWriteProperty<>(12);
    ObservableProperty<Integer> property2 = new ObservableReadWriteProperty<>(4);
    ObservableProperty<Integer> compound
        = new CompoundObservableProperty<>(property1, property2, converter);

    Assert.assertEquals("The converter gets called by the constructor",
        2, converterCallCount.get());
    Assert.assertEquals("Calling get() returns the value from the converter",
        Integer.valueOf(3), compound.get());
    Assert.assertEquals(2, converterCallCount.get());

    property1.set(16);
    Assert.assertEquals("Changing the value of a dependency property triggers a converter call",
        3, converterCallCount.get());
    Assert.assertEquals(Integer.valueOf(4), compound.get());
    Assert.assertEquals(3, converterCallCount.get());

    property2.set(8);
    Assert.assertEquals("Changing the value of a dependency property triggers a converter call",
        4, converterCallCount.get());
    Assert.assertEquals(Integer.valueOf(2), compound.get());
  }

  @Test
  public void testSetConverter() {
    AtomicInteger converterCallCount = new AtomicInteger(0);
    BinaryOperator<Integer> converter = (a, b) -> {
      converterCallCount.incrementAndGet();
      return a - b;
    };

    CompoundObservableProperty<Integer, Integer, Integer> prop = new CompoundObservableProperty<>(
        new ObservableReadWriteProperty<>(5),
        new ObservableReadWriteProperty<>(3),
        (p1, p2) -> 1
    );
    prop.setConverter(converter);

    Assert.assertEquals("The converter is called", 1, converterCallCount.get());
    Assert.assertEquals("The value returned by the new converter is used",
        Integer.valueOf(2), prop.get());
  }

}
