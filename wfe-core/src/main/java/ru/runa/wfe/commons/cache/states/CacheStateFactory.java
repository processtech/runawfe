package ru.runa.wfe.commons.cache.states;

import ru.runa.wfe.commons.cache.CacheImplementation;

/**
 * Factory for creating states for cache state machine.
 *
 * @param <CacheImpl>
 *            Cache implementation type.
 * @param <StateContext>
 *            States context type.
 */
public interface CacheStateFactory<CacheImpl extends CacheImplementation, StateContext> {

    /**
     * Creates empty cache state. No cache initialized or initializing. No dirty transactions exists.
     *
     * @param cache
     *            Cache, which may be returned until initialization.
     * @param context
     *            Optional context for state creation.
     * @return Return cache state machine state.
     */
    public CacheState<CacheImpl, StateContext> createEmptyState(CacheImpl cache, StateContext context);

    /**
     * Creates cache state for cache lazy initialization.
     *
     * @param cache
     *            Cache proxy, returned by state until cache initialization complete.
     * @param context
     *            Optional context for state creation.
     * @return Return cache state machine state.
     */
    public CacheState<CacheImpl, StateContext> createInitializingState(CacheImpl cache, StateContext context);

    /**
     * Creates cache state for initialized, fully operational cache.
     *
     * @param cache
     *            Initialized, fully operational cache instance.
     * @param context
     *            Optional context for state creation.
     * @return Return cache state machine state.
     */
    public CacheState<CacheImpl, StateContext> createInitializedState(CacheImpl cache, StateContext context);

    /**
     * Creates dirty cache state. All dirty transactions is passed to state via {@link DirtyTransactions<T>} parameter.
     *
     * @param cache
     *            Cache, which may be returned by state.
     * @param dirtyTransactions
     *            All dirty transactions.
     * @param context
     *            Optional context for state creation.
     * @return Return cache state machine state.
     */
    public CacheState<CacheImpl, StateContext> createDirtyState(CacheImpl cache, DirtyTransactions<CacheImpl> dirtyTransactions,
            StateContext context);
}
