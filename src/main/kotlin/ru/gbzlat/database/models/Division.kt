package ru.gbzlat.database.models


/*@Serializable
data class Division (
    val id: Int,
    val name: String
)

object Divisions: BaseTable<Division>("Divisions") {
    val id = int("id").primaryKey()
    val name = varchar("name")

    override fun doCreateEntity(row: QueryRowSet, withReferences: Boolean)= Division(
        id = row[id]!!,
        name = row[name]!!
    )
}*/

//val DatabaseManager.divisions get() = database.sequenceOf(Divisions)