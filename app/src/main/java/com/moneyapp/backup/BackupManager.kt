package com.moneyapp.backup

import android.content.Context
import android.net.Uri
import com.moneyapp.data.Transaction
import com.moneyapp.data.TransactionRepository
import com.moneyapp.data.TxStatus
import com.moneyapp.data.TxType
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

@JsonClass(generateAdapter = false)
data class BackupTx(
    val type: String,
    val amount: Long,
    val category: String,
    val merchant: String?,
    val memo: String?,
    val occurredAt: Long,
    val source: String,
    val status: String
)

class BackupManager(
    private val context: Context,
    private val repository: TransactionRepository
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, BackupTx::class.java)
    private val adapter = moshi.adapter<List<BackupTx>>(listType).indent("  ")

    suspend fun exportJson(uri: Uri): Int {
        val txs = repository.allForExport().map { it.toBackup() }
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream, StandardCharsets.UTF_8).use { writer ->
                writer.write(adapter.toJson(txs))
            }
        }
        return txs.size
    }

    suspend fun exportCsv(uri: Uri): Int {
        val txs = repository.allForExport()
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            OutputStreamWriter(stream, StandardCharsets.UTF_8).use { writer ->
                writer.write("type,amount,category,merchant,memo,occurredAt,source,status\n")
                for (t in txs) {
                    writer.write(
                        listOf(
                            t.type.name,
                            t.amount.toString(),
                            csv(t.category),
                            csv(t.merchant.orEmpty()),
                            csv(t.memo.orEmpty()),
                            t.occurredAt.toString(),
                            csv(t.source),
                            t.status.name
                        ).joinToString(",") + "\n"
                    )
                }
            }
        }
        return txs.size
    }

    suspend fun importJson(uri: Uri): Int {
        val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
            ?: return 0
        val items = adapter.fromJson(json).orEmpty()
        for (item in items) {
            repository.add(item.toTransaction())
        }
        return items.size
    }

    private fun csv(s: String): String {
        val needs = s.contains(',') || s.contains('"') || s.contains('\n')
        val escaped = s.replace("\"", "\"\"")
        return if (needs) "\"$escaped\"" else escaped
    }

    private fun Transaction.toBackup() = BackupTx(
        type = type.name,
        amount = amount,
        category = category,
        merchant = merchant,
        memo = memo,
        occurredAt = occurredAt,
        source = source,
        status = status.name
    )

    private fun BackupTx.toTransaction() = Transaction(
        type = TxType.valueOf(type),
        amount = amount,
        category = category,
        merchant = merchant,
        memo = memo,
        occurredAt = occurredAt,
        source = source,
        status = TxStatus.valueOf(status)
    )
}
