package me.chanjar;

import static org.junit.Assert.*;

import java.util.List;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.ActivitiRule;
import org.activiti.engine.test.Deployment;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:springTypicalUsageTest-context.xml")
public class EventSubprocessTest {
    
    @Autowired
    private RepositoryService repositoryService;
  
    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    @Rule
    public ActivitiRule activitiSpringRule;
    
    @Test
    @Deployment(resources = "me/chanjar/eventsubprocess/event-subprocess.bpmn")
    public void messageStartEvent() {
      String processDefinitionKey = "event-subprocess";
      runtimeService.startProcessInstanceByKey(processDefinitionKey);

      Task task1 = taskService.createTaskQuery().processDefinitionKey(processDefinitionKey).taskDefinitionKey("usertask1").singleResult();
      assertNotNull(task1);

      sendMessage(processDefinitionKey);

      Task task3 = taskService.createTaskQuery().processDefinitionKey(processDefinitionKey).taskDefinitionKey("usertask3").singleResult();
      taskService.complete(task3.getId());

      task1 = taskService.createTaskQuery().processDefinitionKey(processDefinitionKey).taskDefinitionKey("usertask1").singleResult();
      taskService.complete(task1.getId());

      assertEquals(0, runtimeService.createProcessInstanceQuery().processDefinitionKey(processDefinitionKey).count());
    }
    
    private void sendMessage(String processDefinitionKey) {
      List<Execution> executions = runtimeService
          .createExecutionQuery()
          .processDefinitionKey(processDefinitionKey)
          .messageEventSubscriptionName("abc")
          .list();
      for(Execution execution : executions) {
        runtimeService.messageEventReceived("abc", execution.getId());
      }
      
    }

}
