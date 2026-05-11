package com.moneyapp.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter fun fromType(t: TxType): String = t.name
    @TypeConverter fun toType(s: String): TxType = TxType.valueOf(s)

    @TypeConverter fun fromStatus(s: TxStatus): String = s.name
    @TypeConverter fun toStatus(s: String): TxStatus = TxStatus.valueOf(s)
}
