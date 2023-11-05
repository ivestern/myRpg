package Game

import com.sun.org.apache.xpath.internal.compiler.Keywords
import com.varabyte.kotter.foundation.input.*
import com.varabyte.kotter.foundation.liveVarOf
import com.varabyte.kotter.foundation.text.bold
import com.varabyte.kotter.foundation.text.p
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import java.util.Locale
import kotlin.random.Random

val actionsList = listOf("merchant", "monster", "itemPicked")
val Battle = listOf("Атаковать", "Открыть инвентарь", "Сбежать")
val Merchant = listOf("Купить", "Продать")
var cursorIndex = 0
fun convertToEnglish(key: Key): Key {
    val russianToEnglishMap = mapOf(
        CharKey('й') to CharKey('q'), CharKey('ц') to CharKey('w'),
        CharKey('у') to CharKey('e'), CharKey('к') to CharKey('r'),
        CharKey('е') to CharKey('t'), CharKey('н') to CharKey('y'),
        CharKey('г') to CharKey('u'), CharKey('ш') to CharKey('i'),
        CharKey('щ') to CharKey('o'), CharKey('з') to CharKey('p'),
        CharKey('х') to CharKey('['), CharKey('ъ') to CharKey(']'),
        CharKey('ф') to CharKey('a'), CharKey('ы') to CharKey('s'),
        CharKey('в') to CharKey('d'), CharKey('а') to CharKey('f'),
        CharKey('п') to CharKey('g'), CharKey('р') to CharKey('h'),
        CharKey('о') to CharKey('j'), CharKey('л') to CharKey('k'),
        CharKey('д') to CharKey('l'), CharKey('ж') to CharKey(';'),
        CharKey('э') to CharKey('\''), CharKey('я') to CharKey('z'),
        CharKey('ч') to CharKey('x'), CharKey('с') to CharKey('c'),
        CharKey('м') to CharKey('v'), CharKey('и') to CharKey('b'),
        CharKey('т') to CharKey('n'), CharKey('ь') to CharKey('m'),
        CharKey('б') to CharKey(','), CharKey('ю') to CharKey('.')
    )

    return russianToEnglishMap[key] ?: key
}

fun Session.move(chel: Player): Triple<String, String, String> {
    var actions by liveVarOf("")
    var result by liveVarOf("Управляйте персонажем с помощью W A S D")
    var notice by liveVarOf("")
    var itemsList by liveVarOf("")
    section {
        if (itemsList ==""){
            textLine("Exit <Esc>")
        if (notice!="" && output.first == "chest"){textLine(mergeStrings(createFramedText(result,50,10),createFramedText(notice,40,10),30)) }
        else if(notice!="") {textLine(mergeStrings(createFramedText(result,50,10),createFramedText(notice,35,5),30)) }
        else textLine(createFramedText(result,50,10))
        if (actions!="") { textLine(createFramedText(actions,30,5)) }}
        else{textLine(itemsList)}
    }.runUntilKeyPressed(Keys.ESC) {
        onKeyPressed {
            actions=""
            notice=""
            val keyEngl=convertToEnglish(key)
            output = chel.keyboardHandler(keyEngl)
            notice= output.third
            actions= actionsChooser(output.first,keyEngl,chel)
            if (actions in Battle|| actions in Merchant) when(actions) {
                "Сбежать" -> {
                    chel.posBlocked = false
                    val preResult=chel.keyboardHandler(chel.previousDirect)
                    actions= actionsChooser(preResult.first,Keys.S,chel)
                    val lostMoney = 10*chel.hardLevel
                    if (chel.money>=lostMoney) {chel.money -=lostMoney
                    notice="Вы сбегаете потеряв $lostMoney монет"}
                    else  notice="Вы сбегаете"
                    notice=preResult.third
                    result = preResult.second
                }
                "Открыть инвентарь"->{
                    chel.itemsView()
                }
            }
            else result = output.second
        }
    }
    return output
}

fun chooseGen(actions: List<String>,cursorIndex: Int): String {
    var finalString = ""
    actions.forEachIndexed { index, item ->
        run {
            //val chooser = (if (index == cursorIndex) '>' else ' ')
            val post = (if (index == cursorIndex) "  <E>" else "")
            finalString += "$item$post\n"
        }
    }
    return finalString

}
fun actionsChooser(outputSecond:String,key: Key,chel: Player): String {
    val actions:String
    if (outputSecond in actionsList) {
        chel.posBlocked = true
        actions= when (outputSecond) {
            //"merchant" -> choose(Merchant)
            "monster" -> chooseHandler(key)

            else -> ""
        }
    }
    else actions=""
    return actions
}
fun chooseHandler(key: Key): String {
    when (key) {
        Keys.W -> {
            cursorIndex -= 1
        }
        Keys.D -> {
            cursorIndex += 1
        }
        Keys.S -> {
            cursorIndex += 1
        }
        Keys.A -> {
            cursorIndex -= 1
        }

        Keys.E -> return Battle[cursorIndex]
        else -> {}
    }
    if (cursorIndex < 0) cursorIndex = Battle.lastIndex
    else if (cursorIndex > Battle.lastIndex) cursorIndex = 0
    val actions: String = chooseGen(Battle, cursorIndex)
    return actions
}
fun createFramedText(text: String, width: Int, height: Int): String {
    val lines = text.split("\n")
    val maxWidth = lines.map { it.length }.max() ?: 0

    if (maxWidth > width - 2) {
        throw IllegalArgumentException("Дохуя хочешь") //Text lines are too long to fit in the specified frame width
    }

    val horizontalBorder = "+${"-".repeat(width - 2)}+"
    val emptyLine = "|${" ".repeat(width - 2)}|"

    val remainingSpace = height - 2 - lines.size
    val topPadding = remainingSpace / 2
    val bottomPadding = remainingSpace - topPadding

    val framedText = mutableListOf<String>()

    framedText.add(horizontalBorder)
    for (i in 0..<topPadding) {
        framedText.add(emptyLine)
    }

    for (line in lines) {
        val padding = (width - 2 - line.length) / 2
        val centeredLine = "|${" ".repeat(padding)}$line${" ".repeat(width - 2 - padding - line.length)}|"
        framedText.add(centeredLine)
    }

    for (i in 0..<bottomPadding) {
        framedText.add(emptyLine)
    }
    framedText.add(horizontalBorder)

    return framedText.joinToString("\n")
}
fun mergeStrings(str1: String, str2: String, spacesBetween: Int): String {
    val lines1 = str1.split("\n")
    val lines2 = str2.split("\n")
    val maxLength = maxOf(lines1.maxBy { it.length }?.length ?: 0, lines2.maxBy { it.length }?.length ?: 0)

    val mergedLines = mutableListOf<String>()

    for (i in 0..<maxOf(lines1.size, lines2.size)) {
        val line1 = if (i < lines1.size) lines1[i] else ""
        val line2 = if (i < lines2.size) lines2[i] else ""
        val mergedLine = line1.padEnd(maxLength) + " ".repeat(spacesBetween) + line2
        mergedLines.add(mergedLine)
    }

    return mergedLines.joinToString("\n")
}



interface Event {
    val hardLevel: Int
    fun viewEvent(): String
    val type: String
}

interface Monster : Event {
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

interface Loot {

    val hardLevel: Int
    val cost: Int
        get() = hardLevel * 100
    val name: String
}

interface Armor : Loot {
    val dodge: Double
    val defence: Int
    val type: String
    override val name: String
        get() = "$type броня+${hardLevel}"
}
class ArmorLeather(override val hardLevel: Int) : Armor {
    override val type = "Кожаная"
    override val dodge = 0.3
    override val defence = hardLevel + 1
}

class ArmorChain(override val hardLevel: Int) : Armor {
    override val type = "Кольчужная"
    override val dodge = 0.2
    override val defence = hardLevel + 2
}

class ArmorNone(override val hardLevel: Int = 1) : Armor {
    override val type = ""
    override val dodge = 0.3
    override val defence = 0
}

class ArmorSteel(override val hardLevel: Int) : Armor {
    override val type = "Латная"
    override val dodge = 0.1
    override val defence = hardLevel + 3
}

interface Weapon : Loot {
    val missChance: Double
    val weaponTypes: List<String>
        get() = listOf("Деревянный", "Железный", "Серебряный", "Магический")
    val attack: Int
    val type: String
        get() = (if (hardLevel < 4) {
            weaponTypes[hardLevel - 1]
        } else {
            weaponTypes[3]
        }).toString()
    override val name: String
}

class Staff(override val hardLevel: Int) : Weapon {
    override val name: String = "$type посох+${hardLevel}"
    override val missChance: Double = 0.3
    override val attack: Int = hardLevel + 4
}

class Sword(override val hardLevel: Int) : Weapon {
    override val name: String = "$type меч+${hardLevel}"
    override val missChance: Double = 0.2
    override val attack: Int = hardLevel + 3
}

class Knife(override val hardLevel: Int) : Weapon {
    override val name: String = "$type кинжал+${hardLevel}"
    override val missChance: Double = 0.1
    override val attack: Int = hardLevel + 2
}

class WeaponNone(override val hardLevel: Int = 1) : Weapon {
    override val name: String = ""
    override val missChance: Double = 0.2
    override val attack: Int = 2
}

data class Money(val money: Int) : Loot {
    override val hardLevel: Int
        get() = TODO("Not yet implemented")
    override val name = "$money монет"
}

interface Potion : Loot {
    val type: String
    override val name: String
        get() = "Зелье ${type}+${hardLevel}"
}

class Nothing(override val hardLevel: Int = 0) : Loot {
    override val name: String = "Пусто"
}

class Empty(override val hardLevel: Int = 0) : Event {
    override fun viewEvent(): String {
        return "Здесь пусто"
    }

    override val type = "empty"
}

class Magazin(override val hardLevel: Int = 0) : Event {
    override fun viewEvent(): String {
        return "Перед вами торговец"
    }

    override val type = "merchant"
}

class Chest(override val hardLevel: Int) : Event {
    override val type = "chest"
    private fun generateLoot(hardLevel: Int): Loot {
        val lootList = listOf(
            Staff(hardLevel), Knife(hardLevel), Sword(hardLevel),
            ArmorChain(hardLevel), ArmorLeather(hardLevel), ArmorSteel(hardLevel),
            Money(Random.nextInt(10 + hardLevel * 20, 100 + hardLevel * 20))
        )
        return lootList.random()

    }

    var loot = this.generateLoot(hardLevel)
    var state: Int = 0
    override fun viewEvent(): String {
        when (state) {
            2 -> return "Перед вами пустой сундук"
            1 -> {
                return "В сундуке лежит ${loot.name}\n нажмите E чтобы взять/экипировать"
            }

            0 -> {
                return "Перед вами сундук, чтобы открыть нажмите Q"
            }
        }
        return "chest ok"
    }
}

class Portal(override val hardLevel: Int) : Event {
    override val type = "portal"
    override fun viewEvent(): String {
        return "Перед вами портал, нажмите E чтобы войти"
    }

}

data class Slime(val color: String, override val hardLevel: Int) : Monster {
    override val name: String = "Слизень"
    override val attack: Int = 3
    override val health: Int = 10
    override var stateLive: Boolean = true
}

data class GoblinSword(override val hardLevel: Int) : Monster {
    override val name: String = "Гоблин мечник"
    override val attack: Int = 5
    override val health: Int = 20
    override var stateLive: Boolean = true
}

data class GoblinBow(override val hardLevel: Int) : Monster {
    override val name: String = "Гоблин лучник"
    override val attack: Int = 7
    override val health: Int = 15
    override var stateLive: Boolean = true
}

data class Orc(override val hardLevel: Int) : Monster {
    override val name: String = "Орк"
    override val attack: Int = 9
    override val health: Int = 30
    override var stateLive: Boolean = true
}

var output = Triple("empty","","")
val slimeColors = listOf("Зеленый", "Синий", "Красный", "Желтый")
var map = mutableMapOf<Pair<Int?, Int?>, Event?>(Pair(0, 0) to Empty())

class Player(val name: String) {
    val health = 10
    var armor = ArmorNone() as Armor
    var weapon = WeaponNone() as Weapon
    var defence = armor.defence
    val attack = weapon.attack
    val dodge = armor.dodge
    var x = 0
    var y = 0
    lateinit var previousDirect:Key
    var hardLevel = 1
    var keyCount = 0
    var money = 0
    var pos = Pair(this.x, this.y)
    var posBlocked = false
    var items:MutableMap<Int,Loot> = mutableMapOf()
    fun itemsView(){
        for (items in items){

        }
    }
    private fun move(key: Key): Boolean {
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
        previousDirect=key
        return true
    }

    private fun interact(key: Key, lastEvent: Event?): Triple<String,String,String> {
        when (key) {
            Keys.E -> {
                return eventHandler(lastEvent)
            }

            Keys.Q -> {
                if (lastEvent is Chest) {
                    lastEvent.state = 1
                    return Triple(lastEvent.type,lastEvent.viewEvent(), "")
                } else return Triple(lastEvent!!.type,lastEvent.viewEvent(),"Вы не можете сделать это",)
            }

            else -> if ((key ==Keys.W || key ==Keys.A || key ==Keys.S|| key ==Keys.D)&&posBlocked==true) return Triple(lastEvent!!.type,"","Вы не можете уйти из боя просто так" )
            else {}
        }
        return Triple("1","Adios","")
    }

    fun keyboardHandler(key: Key): Triple<String, String,String> {
        val lastEvent = map[pos]
        if (posBlocked == false) {
            if (move(key) == false) return interact(key, lastEvent)
        }
        pos = Pair(this.x, this.y)
        if (pos in map) {
            output = Triple(map[pos]!!.type,"Position: ${pos.second}, ${pos.first} \n ${map[pos]?.viewEvent()}\n","")
        } else {
            output = eventGenerator(hardLevel, pos)
        }
        return output
    }

    private fun eventHandler(lastEvent: Event?): Triple<String,String, String> {
        when (lastEvent) {
            is Portal -> {
                hardLevel++
                map = mutableMapOf(Pair(0, 0) to Empty())
                this.x = 0
                this.y = 0
                return Triple(Empty().type,"Position: 0,0\n${Empty().viewEvent()}","Вы прошли через портал\nЧто то изменилось")
            }

            is Chest -> {
                lateinit var actionResult: String
                when (lastEvent.loot) {
                    is Armor -> {
                        this.armor = lastEvent.loot as Armor
                        actionResult = ("Вы экипировали ${lastEvent.loot.name}")
                    }

                    is Weapon -> {
                        this.weapon = lastEvent.loot as Weapon
                        actionResult = ("Вы экипировали ${lastEvent.loot.name}")
                    }

                    is Money -> {
                        this.money = (lastEvent.loot as Money).money
                        actionResult = ("Вы взяли ${lastEvent.loot.name}")
                    }

                    is Nothing -> {
                        actionResult = ("Брать нечего")
                    }
                }
                if (lastEvent.state == 1) lastEvent.loot = Nothing()
                if (lastEvent.state < 2) lastEvent.state += 1
                return Triple(lastEvent.type,lastEvent.viewEvent(),actionResult)

            }

            is Monster -> {
                return Triple(lastEvent.type,lastEvent.viewEvent(),"")
            }

            is Empty -> {
                return Triple(lastEvent.type,lastEvent.viewEvent(),"Взаимодействовать не с чем")
            }

            else -> return Triple("","", "")

        }
    }

}

fun eventGenerator(hardLevel: Int, pos: Pair<Int, Int>): Triple<String,String, String> {
    val events = listOf("nothing", "monster", "chest", "portal")
    val event: Event? = when (events.random()) {
        "nothing" -> Empty()
        "monster" -> listOf(
            Slime(slimeColors.random(), hardLevel),
            GoblinBow(hardLevel),
            GoblinSword(hardLevel),
            Orc(hardLevel)
        ).random()

        "chest" -> Chest(hardLevel)
        "portal" -> Portal(hardLevel)
        else -> null
    }
    map.put(pos, event)
    return Triple(event!!.type,"Position: ${pos.second}, ${pos.first} \n ${event?.viewEvent()}\n", "")
}