// Copyright © Microsoft Corporation
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//      http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.indexerqueue.azure.scope.thread;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThreadScopeContextTest {

    final static private String name = "name";

    @Mock
    Object object;

    @Mock
    Runnable runnable;

    @InjectMocks
    ThreadScopeContext sut = new ThreadScopeContext();

    @Test
    void should_returnNull_whenKeyNotSetInGetBean() {
        assertNull(sut.getBean(name));
    }

    @Test
    void should_returnSameObjectAddedInSetBean_whenGetBeanCalled() {
        sut.setBean(name, object);
        Object response = sut.getBean(name);

        assertEquals(object, response);
    }

  @Test
  void should_returnNull_whenRemoveCalledOnAbsentBean() {
    assertNull(sut.remove(name));
  }

  @Test
  void should_returnBeanObjectAndDeleteBean_whenRemoveCalledOnBean() {
      sut.setBean(name, object);
      Object response = sut.remove(name);

      assertEquals(object, response);
      assertNull(sut.getBean(name));
  }

    @Test
    void should_setDestructionCallBack_whenRegisterDestructionCallback_isCalled() {
      doNothing().when(runnable).run();
      sut.registerDestructionCallback(name, runnable);
      sut.remove(name);

      verify(runnable,times(1)).run();
    }

    @Test
    void should_clearBean_whenClearCalled() {
        sut.setBean(name, object);
        sut.clear();

        assertNull(sut.getBean(name));
    }

    @Test
    void should_callDestructionCallBack_whenClearCalled() {
      doNothing().when(runnable).run();
      sut.setBean(name, object);
      sut.registerDestructionCallback(name,runnable);
      sut.clear();

      verify(runnable,times(1)).run();
    }
}
