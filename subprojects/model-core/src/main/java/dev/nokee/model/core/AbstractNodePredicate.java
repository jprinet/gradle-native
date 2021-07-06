package dev.nokee.model.core;

import static java.util.Objects.requireNonNull;

abstract class AbstractNodePredicate<T> implements NodePredicate<T> {
	private final ModelSpec<T> matcher;
	private final NodePredicateScopeStrategy scopeStrategy;

	protected AbstractNodePredicate(ModelSpec<T> matcher, NodePredicateScopeStrategy scopeStrategy) {
		this.matcher = requireNonNull(matcher);
		this.scopeStrategy = scopeStrategy;
	}

	public final ModelSpec<T> scope(ModelNode node) {
		return scopeStrategy.scope(node, matcher);
	}
}