package ru.runa.wfe.user.jaxb;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.TransactionalExecutor;
import ru.runa.wfe.user.Executor;

public class ExecutorAdapter extends XmlAdapter<WfExecutor, Executor> {
    private static final Log log = LogFactory.getLog(ExecutorAdapter.class);

    @Override
    public WfExecutor marshal(Executor executor) {
        WfExecutor wfExecutor = new WfExecutor();
        wfExecutor.setId(executor.getId());
        wfExecutor.setExecutorClassName(executor.getClass().getName());
        try {
            BeanUtils.copyProperties(wfExecutor, executor);
        } catch (Exception e) {
            log.error("ws conversion error", e);
        }
        return wfExecutor;
    }

    @Override
    public Executor unmarshal(WfExecutor executor) {
        TransactionalExecutorLoader loader = new TransactionalExecutorLoader(executor.getId());
        loader.executeInTransaction(true);
        return loader.executor;
    }

    @RequiredArgsConstructor
    private class TransactionalExecutorLoader extends TransactionalExecutor {
        final Long executorId;
        Executor executor;

        @Override
        protected void doExecuteInTransaction() throws Exception {
            executor = ApplicationContextFactory.getExecutorDAO().getExecutor(executorId);
        }

    }

}
