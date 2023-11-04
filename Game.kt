package Game
import com.varabyte.kotter.foundation.input.Key
import com.varabyte.kotter.foundation.input.Keys
import com.varabyte.kotter.foundation.input.onKeyPressed
import com.varabyte.kotter.foundation.input.runUntilKeyPressed
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.render.aside
import com.varabyte.kotter.foundation.runUntilSignal
import com.varabyte.kotter.foundation.session
import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Section
import com.varabyte.kotter.runtime.Session
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import kotlinx.coroutines.delay
import org.jline.terminal.Terminal
import java.util.Base64
import kotlin.random.Random
val actionsList= listOf("merchant","monster","itemPicked")
    val Battle = listOf("Атаковать","Открыть инвентарь","Сбежать")
    val Merchant = listOf("Купить","Продать")
    fun Session.move(chel: Player, direction: Key? = null): Pair<String, String> {
        var actions by liveVarOf("")
        var result by liveVarOf("Управляйте персонажем с помощью W A S D")
        if (direction != null) output = chel.keyboardHandler(direction)
        section {
            bordered(
                BorderCharacters.ASCII,
                paddingLeftRight = 1,
                paddingTopBottom = 1
            ) { textLine(result) }
            bold { textLine(actions) }
        }.runUntilKeyPressed(Keys.ESC) {
            onKeyPressed {
                if (direction == null) output = chel.keyboardHandler(key)
                result = output.first
                if (output.second in actionsList) {
                    var cursorIndex by liveVarOf(0)
                    chel.posBlocked = true
                    val action = when (output.second) {
                        //"merchant" -> choose(Merchant)
                        "monster" -> {
                            chooseHandler(key, cursorIndex)
                        }

                        else -> ""
                    }
                }
            }
        }
        return output
    }
fun chooseGen(actions: List<String>,cursorIndex: Int): String {
        var finalString = ""
        actions.forEachIndexed { index, item ->
            run {
                val chooser = (if (index == cursorIndex) '>' else ' ')
                finalString += "$item+$chooser\n"
            }
        }
        return finalString

    }
fun chooseHandler(key: Key, cursorIndex: Int) {
        var cursorIndex = cursorIndex
        var actions = chooseGen(Battle, cursorIndex)
        var test = ""
        when (key) {
            Keys.UP -> {
                cursorIndex -= 1
                actions = chooseGen(Battle, cursorIndex)
            }

            Keys.DOWN -> {
                cursorIndex += 1
                actions = chooseGen(Battle, cursorIndex)
            }

            Keys.SPACE -> test = Battle[cursorIndex]
            else -> {}
        }
        if (cursorIndex < 0) cursorIndex = Battle.lastIndex
        else if (cursorIndex > Battle.lastIndex) cursorIndex = 0 else TODO()
    }
/*fun Session.actionHandler(output: Pair<String,String>,chel: Player) {


    val result = when(action){
        "Сбежать"-> move(chel,Keys.W)
        else -> {}
    }
}*/
interface Actions{
    val descr: String
}

interface Event{
    val hardLevel:Int
    fun viewEvent(): String
    val type: String
}

interface Monster: Event{
    val name: String
    val attack: Int
    val health: Int
    var stateLive: Boolean
    override val type
        get() = "monster"
    override fun viewEvent(): String {
        return "Перед вами ${this.name} ${this.hardLevel} уровня" +
                "\nАтака: ${this.attack}" +
                "\nЗдоровье: ${this.health}"
    }
}
interface Loot{

    val hardLevel: Int
    val cost: Int
        get() = hardLevel*100
    val name: String
}
interface Armor:Loot{
    val dodge: Double
    val defence: Int
    val type: String
    override val name: String
        get()="$type броня+${hardLevel}"
}

class ArmorLeather(override val hardLevel: Int) :Armor{
    override val type = "Кожаная"
    override val dodge = 0.3
    override val defence = hardLevel+1
}
class ArmorChain(override val hardLevel: Int) :Armor{
    override val type = "Кольчужная"
    override val dodge = 0.2
    override val defence = hardLevel+2
}
class ArmorNone(override val hardLevel: Int =1) :Armor{
    override val type = ""
    override val dodge = 0.3
    override val defence = 0
}
class ArmorSteel(override val hardLevel: Int) :Armor{
    override val type = "Латная"
    override val dodge = 0.1
    override val defence = hardLevel+3
}
interface Weapon:Loot{
    val missChance: Double
    val weaponTypes:  List<String>
        get() = listOf("Деревянный", "Железный","Серебряный","Магический")
    val attack:Int
    val type: String
        get() = (if (hardLevel<4){weaponTypes[hardLevel-1]} else {
            weaponTypes[3]
        }).toString()
    override val name: String
}
class Staff(override val hardLevel: Int):Weapon{
    override val name: String = "$type посох+${hardLevel}"
    override val missChance: Double = 0.3
    override val attack: Int = hardLevel+4
}
class Sword(override val hardLevel: Int):Weapon{
    override val name: String = "$type меч+${hardLevel}"
    override val missChance: Double = 0.2
    override val attack: Int = hardLevel+3
}
class Knife(override val hardLevel: Int):Weapon{
    override val name: String = "$type кинжал+${hardLevel}"
    override val missChance: Double = 0.1
    override val attack: Int = hardLevel+2
}
class WeaponNone(override val hardLevel: Int=1):Weapon{
    override val name: String = ""
    override val missChance: Double = 0.2
    override val attack: Int = 2
}

data class Money(val money:Int): Loot {
    override val hardLevel: Int
        get() = TODO("Not yet implemented")
    override val name = "$money монет"
}
interface Potion:Loot{
    val type: String
    override val name: String
        get()="Зелье ${type}+${hardLevel}"
}
class Nothing(override val hardLevel: Int=0):Loot{
    override val name: String="Пусто"
}

class Empty(override val hardLevel: Int=0): Event{
    override fun viewEvent(): String {
        return "Здесь пусто"
    }

    override val type = "empty"
}
class Magazin(override val hardLevel: Int=0): Event{
    override fun viewEvent(): String {
        return "Перед вами торговец"
    }
    override val type = "merchant"
}
class Chest(override val hardLevel: Int):Event{
    override val type = "chest"
    private fun generateLoot(hardLevel: Int):Loot {
        val lootList = listOf(
            Staff(hardLevel),Knife(hardLevel),Sword(hardLevel),
            ArmorChain(hardLevel),ArmorLeather(hardLevel),ArmorSteel(hardLevel),
            Money(Random.nextInt(10+hardLevel*20, 100+hardLevel*20))
        )
        return lootList.random()

    }
    var loot = this.generateLoot(hardLevel)
    var state: Int = 0
    override fun viewEvent(): String {
        when(state){
            2-> return "Перед вами пустой сундук"
            1->{
                return "В сундуке лежит ${loot.name}, нажмите E чтобы взять/экипировать"
            }
            0 -> {
                return "Перед вами сундук, чтобы открыть нажмите Q"}
        }
        return "chest ok"
    }
}
class Portal(override val hardLevel: Int): Event {
    override val type = "portal"
    override fun viewEvent(): String {
        return "Перед вами портал, нажмите E чтобы войти"
    }

}

data class Slime(val color: String, override val hardLevel: Int): Monster{
    override val name: String = "Слизень"
    override val attack: Int = 3
    override val health: Int = 10
    override var stateLive: Boolean = true
}
data class GoblinSword(override val hardLevel: Int): Monster{
    override val name: String = "Гоблин мечник"
    override val attack: Int = 5
    override val health: Int = 20
    override var stateLive: Boolean = true
}
data class GoblinBow(override val hardLevel: Int): Monster{
    override val name: String = "Гоблин лучник"
    override val attack: Int = 7
    override val health: Int = 15
    override var stateLive: Boolean = true
}
data class Orc(override val hardLevel: Int): Monster{
    override val name: String = "Орк"
    override val attack: Int = 9
    override val health: Int = 30
    override var stateLive: Boolean = true
}

var output = Pair("","empty")
val slimeColors = listOf("Зеленый", "Синий", "Красный", "Желтый")
var map = mutableMapOf<Pair<Int?,Int?>,Event?>(Pair(0,0) to Empty())
class Player(val name: String) {
    val health = 10
    var armor = ArmorNone() as Armor
    var weapon = WeaponNone() as Weapon
    var defence = armor.defence
    val attack = weapon.attack
    val dodge = armor.dodge
    var x = 0
    var y = 0
    var hardLevel = 1
    var keyCount = 0
    var money = 0
    var pos = Pair(this.x, this.y)
    var posBlocked = false
    fun move(key: Key):Boolean {
        when (key) {
            Keys.W -> {
                this.y++
            }

            Keys.A -> {
                this.x--
            }

            Keys.S -> {
                this.y--
            }

            Keys.D -> {
                this.x++
            }
            else -> return false
        }
        return true
    }
    fun interact(key: Key,lastEvent: Event?):Pair<String,String>{
        when(key){
            Keys.E -> {
                return eventHandler(lastEvent)
            }
            Keys.Q-> {
                if (lastEvent is Chest) {
                    lastEvent.state=1
                    return Pair(lastEvent.viewEvent(),lastEvent.type)
                } else return Pair("Вы не можете сделать это",lastEvent!!.type)
            }
            else -> return Pair("Вы не можете уйти из боя просто так",lastEvent!!.type)
        }
    }
     fun keyboardHandler(key: Key): Pair<String,String> {
         val lastEvent = map[pos]
         if (posBlocked==false){
             if (move(key)==false) return interact(key,lastEvent)
        }
         else return interact(key,lastEvent)
         pos=Pair(this.x,this.y)
        if (pos in map) {
            output= Pair("Position: ${pos.second}, ${pos.first} \n ${map[pos]?.viewEvent()}\n", map[pos]!!.type)
        } else {
            output=eventGenerator(hardLevel, pos)
        }
         return output
    }
    private fun eventHandler(lastEvent: Event?): Pair<String,String> {
        when(lastEvent){
            is Portal -> {hardLevel++
                map = mutableMapOf(Pair(0,0)  to Empty())
                this.x = 0
                this.y = 0
                return Pair("Вы прошли через портал, что то изменилось",lastEvent.type)
            }
            is Chest -> {
                lateinit var actionResult:String
                when(lastEvent.loot){
                    is Armor-> {this.armor= lastEvent.loot as Armor
                        actionResult = ("Вы экипировали ${lastEvent.loot.name}")
                    }
                    is Weapon-> {this.weapon= lastEvent.loot as Weapon
                        actionResult = ("Вы экипировали ${lastEvent.loot.name}")
                    }
                    is Money-> {this.money = (lastEvent.loot as Money).money
                        actionResult = ("Вы взяли ${lastEvent.loot.name}")
                    }
                    is Nothing -> {actionResult = ("Брать нечего")
                    }
                }
                if (lastEvent.state==1) lastEvent.loot = Nothing()
                if (lastEvent.state<2) lastEvent.state+=1
                return Pair(actionResult,lastEvent.type)

            }
            is Monster -> {return Pair(lastEvent.viewEvent(),"monster")}
            is Empty -> {
                return Pair("Взаимодействовать не с чем",lastEvent.type)
            }
            else-> return Pair("","")

        }
    }

}
fun eventGenerator(hardLevel: Int,pos:Pair<Int,Int>): Pair<String, String> {
    val events = listOf("nothing","monster","chest","portal")
    val event:Event? = when(events.random()){
        "nothing"->Empty()
        "monster" -> listOf(
            Slime(slimeColors.random(), hardLevel),
            GoblinBow(hardLevel),
            GoblinSword(hardLevel),
            Orc(hardLevel)).random()
        "chest" -> Chest(hardLevel)
        "portal" -> Portal(hardLevel)
        else -> null
    }
    map.put(pos,event)
    return Pair("Position: ${pos.second}, ${pos.first} \n ${event?.viewEvent()}\n", event!!.type)
}