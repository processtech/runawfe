/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.presentation;

/**
 * Presentation class, contains information about persistent class and object properties, what can be used in batch presentation.
 */
public class ClassPresentation {

    /**
     * At DB request HQL/SQL string requested (root persistent) class has this name.
     */
    public static final String classNameSQL = "instance";

    /**
     * Root persistent class of {@link ClassPresentation}. All fields is queried against this object.
     */
    private final Class<?> clazz;

    /**
     * Predefined restrictions for root persistent object. For example {@link BatchPresentation} must returns only objects, with some property set:
     * classNameSQL + ".property is not null".
     */
    private final String classRestrictions;

    /**
     * Fields (properties), available via {@link BatchPresentation}. WFE will support filter/sort only by this fields.
     */
    private final FieldDescriptor[] fields;

    /**
     * Flag, equals true, if paging is enabled for persistent class loading; false otherwise.
     */
    private final boolean withPaging;

    /**
     * Creates class presentation instance.
     *
     * @param clazz
     *            Root persistent class.
     * @param classRestrictions
     *            Predefined restrictions for root persistent object.
     * @param withPaging
     *            Flag, equals true, if paging is enabled for persistent class loading; false otherwise.
     * @param fields
     *            Fields (properties), available via {@link BatchPresentation}.
     */
    public ClassPresentation(Class<?> clazz, String classRestrictions, boolean withPaging, FieldDescriptor[] fields) {
        this.clazz = clazz;
        this.classRestrictions = classRestrictions;
        this.fields = fields;
        this.withPaging = withPaging;
    }

    /**
     * Root persistent class.
     */
    public Class<?> getPresentationClass() {
        return clazz;
    }

    /**
     * Predefined restrictions for root persistent object.
     */
    public String getRestrictions() {
        return classRestrictions;
    }

    /**
     * Fields (properties), available via {@link BatchPresentation}.
     */
    public FieldDescriptor[] getFields() {
        return fields;
    }

    /**
     * @return Flag, equals true, if paging is enabled for persistent class loading; false otherwise.
     */
    public boolean isWithPaging() {
        return withPaging;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj != null && getClass() == obj.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getName().hashCode();
    }
}
