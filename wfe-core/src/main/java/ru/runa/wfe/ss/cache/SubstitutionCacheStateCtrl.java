package ru.runa.wfe.ss.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import ru.runa.wfe.commons.cache.sm.BaseCacheCtrl;
import ru.runa.wfe.commons.cache.sm.CacheInitializationContext;
import ru.runa.wfe.commons.cache.sm.CachingLogic;
import ru.runa.wfe.commons.cache.sm.factories.NonRuntimeCacheFactory;
import ru.runa.wfe.commons.cache.sm.factories.StaticCacheFactory;
import ru.runa.wfe.commons.cache.states.DefaultStateContext;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorGroupMembership;

/**
 * Cache control object for substitutions.
 */
class SubstitutionCacheStateCtrl extends BaseCacheCtrl<ManageableSubstitutionCache, DefaultStateContext> implements SubstitutionCache {

    public SubstitutionCacheStateCtrl() {
        super(new NonRuntimeSubstitutionCacheFactory(), createListenObjectTypes());
        CachingLogic.registerChangeListener(this);
    }

    public SubstitutionCacheStateCtrl(boolean fakeBooleanUseStaticCache) {
        super(new SubstitutionCacheFactory(), createListenObjectTypes());
        CachingLogic.registerChangeListener(this);
    }

    @Override
    public TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor, boolean loadIfRequired) {
        SubstitutionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstitutors(actor, loadIfRequired);
    }

    @Override
    public TreeMap<Substitution, Set<Long>> tryToGetSubstitutors(Actor actor) {
        SubstitutionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstitutors(actor, false);
    }

    @Override
    public Set<Long> getSubstituted(Actor actor) {
        SubstitutionCache cache = CachingLogic.getCacheImpl(stateMachine);
        return cache.getSubstituted(actor);
    }

    private static final List<ListenObjectDefinition> createListenObjectTypes() {
        ArrayList<ListenObjectDefinition> result = new ArrayList<ListenObjectDefinition>();
        result.add(new ListenObjectDefinition(Substitution.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(SubstitutionCriteria.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(ExecutorGroupMembership.class, ListenObjectLogType.BECOME_DIRTY));
        result.add(new ListenObjectDefinition(Executor.class, ListenObjectLogType.BECOME_DIRTY));
        return result;
    }

    /**
     * Static factory. It creates on the fly by demand and it state is always equals to database state. May leads to high delay if many executors and
     * substitutions is used. It's recommend to use {@link NonRuntimeSubstitutionCacheFactory}.
     */
    private static class SubstitutionCacheFactory implements StaticCacheFactory<ManageableSubstitutionCache> {

        @Override
        public ManageableSubstitutionCache buildCache() {
            return new SubstitutionCacheStateImpl(true, false, null);
        }
    }

    /**
     * Non runtime factory. It creates on background and cache state may differs from database state for some time.
     */
    private static class NonRuntimeSubstitutionCacheFactory implements NonRuntimeCacheFactory<ManageableSubstitutionCache> {

        @Override
        public ManageableSubstitutionCache createProxy() {
            return new SubstitutionCacheStateImpl(false, true, null);
        }

        @Override
        public ManageableSubstitutionCache buildCache(CacheInitializationContext<ManageableSubstitutionCache> context) {
            return new SubstitutionCacheStateImpl(true, true, context);
        }
    }
}
