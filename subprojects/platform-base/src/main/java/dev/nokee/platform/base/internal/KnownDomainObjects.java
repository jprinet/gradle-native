package dev.nokee.platform.base.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.KnownDomainObject;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;

public class KnownDomainObjects {
	public static final class Existing<T> implements KnownDomainObject<T> {
		private final DomainObjectIdentifier identity;
		private final Class<T> type;
		private final T element;

		public Existing(DomainObjectIdentifier identity, Class<T> type, T element) {
			this.identity = identity;
			this.type = type;
			this.element = element;
		}

		@Override
		public void configure(Action<? super T> action) {
			action.execute(element);
		}

		@Override
		public Class<T> getType() {
			return type;
		}

		@Override
		public DomainObjectIdentifier getIdentity() {
			return identity;
		}

		@Override
		public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
			return null;
		}

		@Override
		public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
			return null;
		}
	}

	public static final class Providing<T> implements KnownDomainObject<T> {
		@Getter private final DomainObjectIdentifier identity;
		@Getter private final Class<T> type;
		private final NamedDomainObjectProvider<DomainObjectElement<T>> delegate;

		public Providing(DomainObjectIdentifier identity, Class<T> type, NamedDomainObjectProvider<DomainObjectElement<T>> delegate) {
			this.identity = identity;
			this.type = type;
			this.delegate = delegate;
		}

		@Override
		public void configure(Action<? super T> action) {
			delegate.configure(it -> action.execute(it.get()));
		}

		@Override
		public <S> Provider<S> map(Transformer<? extends S, ? super T> transformer) {
			return delegate.map(it -> transformer.transform(it.get()));
		}

		@Override
		public <S> Provider<S> flatMap(Transformer<? extends Provider<? extends S>, ? super T> transformer) {
			return delegate.flatMap(it -> transformer.transform(it.get()));
		}
	}
}
