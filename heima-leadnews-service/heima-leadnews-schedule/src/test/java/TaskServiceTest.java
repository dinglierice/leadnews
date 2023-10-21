import com.alibaba.fastjson.JSON;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.schedule.ScheduleApplication;
import com.heima.schedule.service.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @description：TODO
 * @author：dinglie
 * @date：2023/10/21 10:48
 */
@SpringBootTest(classes = ScheduleApplication.class)
@RunWith(SpringRunner.class)
public class TaskServiceTest {
    @Resource
    TaskService taskService;

    @Test
    public void addTest(){
        Task task = new Task();
        task.setTaskType(100);
        task.setPriority(59);
        task.setParameters("task_test".getBytes());
        task.setExecuteTime(new Date().getTime() + 500);

        long l = taskService.addTask(task);


        Task task1 = new Task();
        task1.setTaskType(100);
        task1.setPriority(59);
        task1.setParameters("task_test".getBytes());
        task1.setExecuteTime(new Date().getTime() + 600);
        taskService.addTask(task1);

        Task task12 = new Task();
        task12.setTaskType(100);
        task12.setPriority(59);
        task12.setParameters("task_test".getBytes());
        task12.setExecuteTime(new Date().getTime() + 700);
        taskService.addTask(task12);

        System.out.println(l);
    }

    @Test
    public void cancelTask(){
        taskService.cancelTask(1715561618111561730L);
    }

    @Test
    public void testPull() {
        taskService.pull(100, 59);
    }

    @Resource
    CacheService cacheService;

    //耗时6151
    @Test
    public  void testPiple1(){
        long start =System.currentTimeMillis();
        for (int i = 0; i <10000 ; i++) {
            Task task = new Task();
            task.setTaskType(1001);
            task.setPriority(1);
            task.setExecuteTime(new Date().getTime());
            cacheService.lLeftPush("1001_1", JSON.toJSONString(task));
        }
        System.out.println("耗时"+(System.currentTimeMillis()- start));
    }


    @Test
    public void testPiple2(){
        long start  = System.currentTimeMillis();
        //使用管道技术
        List<Object> objectList = cacheService.getstringRedisTemplate().executePipelined(new RedisCallback<Object>() {
            @Nullable
            @Override
            public Object doInRedis(RedisConnection redisConnection) throws DataAccessException {
                for (int i = 0; i <10000 ; i++) {
                    Task task = new Task();
                    task.setTaskType(1001);
                    task.setPriority(1);
                    task.setExecuteTime(new Date().getTime());
                    redisConnection.lPush("1001_1".getBytes(), JSON.toJSONString(task).getBytes());
                }
                return null;
            }
        });
        System.out.println("使用管道技术执行10000次自增操作共耗时:"+(System.currentTimeMillis()-start)+"毫秒");
    }
}
