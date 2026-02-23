package com.example.log

import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.ir.builders.irBlockBody
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

class LogIrTransformer(
    private val pluginContext: IrPluginContext
) : IrElementTransformerVoidWithContext() {

    private val logAnnotationFqName = FqName("com.example.log.Log")

    // kotlin.io.println(Any?) を解決
    // parameters は Kotlin 2.x の新 API (valueParameters は deprecated)
    // println は Any? を引数に取る (String 専用のオーバーロードは存在しない)
    @OptIn(UnsafeDuringIrConstructionAPI::class)
    private val printlnSymbol by lazy {
        pluginContext.referenceFunctions(
            CallableId(packageName = FqName("kotlin.io"), callableName = Name.identifier("println"))
        ).single { fn ->
            fn.owner.parameters.size == 1 && fn.owner.parameters[0].type.isNullableAny()
        }
    }

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitClassNew(declaration: IrClass): IrStatement {
        if (!declaration.hasAnnotation(logAnnotationFqName)) return super.visitClassNew(declaration)

        val primaryCtor = declaration.declarations
            .filterIsInstance<IrConstructor>()
            .firstOrNull { it.isPrimary } ?: return super.visitClassNew(declaration)

        val className = declaration.kotlinFqName.shortName().asString()
        primaryCtor.body = buildBodyWithLog(primaryCtor, className, primaryCtor.body)

        return super.visitClassNew(declaration)
    }

    private fun buildBodyWithLog(ctor: IrConstructor, className: String, original: IrBody?): IrBody {
        val builder = DeclarationIrBuilder(pluginContext, ctor.symbol, ctor.startOffset, ctor.endOffset)
        return builder.irBlockBody {
            // println("ClassName initialized") を先頭に挿入
            // arguments は Kotlin 2.x の新 API (putValueArgument は deprecated)
            val call = irCall(printlnSymbol)
            call.arguments[0] = irString("$className initialized")
            +call
            // 元の本体のステートメントを後続に追加
            (original as? IrBlockBody)?.statements?.forEach { +it }
        }
    }
}
