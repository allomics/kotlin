/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.load.java

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.jetbrains.kotlin.utils.extractRadix

fun KotlinType.lexicalCastFrom(value: String): Any? {
    val typeDescriptor = constructor.declarationDescriptor

    val type = this.makeNotNullable()
    val (number, radix) = extractRadix(value)
    return try {
        when {
            KotlinBuiltIns.isBoolean(type) -> value.toBoolean()
            KotlinBuiltIns.isChar(type) -> value.singleOrNull()
            KotlinBuiltIns.isByte(type) -> number.toByteOrNull(radix)
            KotlinBuiltIns.isShort(type) -> number.toShortOrNull(radix)
            KotlinBuiltIns.isInt(type) -> number.toIntOrNull(radix)
            KotlinBuiltIns.isLong(type) -> number.toLongOrNull(radix)
            KotlinBuiltIns.isFloat(type) -> value.toFloatOrNull()
            KotlinBuiltIns.isDouble(type) -> value.toDoubleOrNull()
            KotlinBuiltIns.isString(type) -> value
            typeDescriptor is ClassDescriptor && typeDescriptor.kind == ClassKind.ENUM_CLASS -> {
                val descriptor = typeDescriptor.unsubstitutedInnerClassesScope.getContributedClassifier(
                        Name.identifier(value),
                        NoLookupLocation.FROM_BACKEND
                )
                if (descriptor is ClassDescriptor && descriptor.kind == ClassKind.ENUM_ENTRY) descriptor else null
            }
            else -> null
        }
    } catch (e: IllegalArgumentException) {
        null
    }
}