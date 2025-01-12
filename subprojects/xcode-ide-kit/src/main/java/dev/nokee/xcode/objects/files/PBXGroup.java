/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.xcode.objects.files;

import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of files in Xcode's virtual filesystem hierarchy.
 */
public final class PBXGroup extends PBXReference {
	private final List<PBXReference> children;

	// Unfortunately, we can't determine this at constructor time, because CacheBuilder
	// calls our constructor and it's not easy to pass arguments to it.
	private SortPolicy sortPolicy;

	private PBXGroup(String name, @Nullable String path, PBXSourceTree sourceTree, List<PBXReference> children) {
		super(name, path, sourceTree);

		this.sortPolicy = SortPolicy.BY_NAME;
		this.children = Lists.newArrayList();
		this.children.addAll(children);
	}

	public List<PBXReference> getChildren() {
		return children;
	}

	public SortPolicy getSortPolicy() {
		return sortPolicy;
	}

	/**
	 * Method by which group contents will be sorted.
	 */
	public enum SortPolicy {
		/**
		 * By name, in default Java sort order.
		 */
		BY_NAME,

		/**
		 * Group contents will not be sorted, and will remain in the
		 * order they were added.
		 */
		UNSORTED;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String name;
		private String path;
		private PBXSourceTree sourceTree = PBXSourceTree.GROUP;
		private final List<PBXReference> children = new ArrayList<>();

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Builder path(String path) {
			this.path = path;
			return this;
		}

		public Builder child(PBXReference reference) {
			children.add(reference);
			return this;
		}

		public Builder sourceTree(PBXSourceTree sourceTree) {
			this.sourceTree = sourceTree;
			return this;
		}

		public Builder children(List<PBXReference> references) {
			this.children.clear();
			this.children.addAll(references);
			return this;
		}

		public PBXGroup build() {
			return new PBXGroup(name, path, sourceTree, children);
		}
	}
}
