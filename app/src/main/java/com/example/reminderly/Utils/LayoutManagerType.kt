package com.example.reminderly.Utils

sealed class RecyclerSealed{
/*object Reminder : RecyclerItemType()
object Header : RecyclerItemType()
object Ad : RecyclerItemType()*/
data class ItemType(val code : Int): RecyclerSealed()

}
