import Game.*
import com.varabyte.kotter.foundation.*
import com.varabyte.kotter.foundation.collections.*
import com.varabyte.kotter.foundation.input.*
import com.varabyte.kotter.foundation.render.aside
import com.varabyte.kotter.foundation.render.offscreen
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.*
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import kotlinx.coroutines.delay
fun main()=session(clearTerminal = true) {
    val chel = Player("Name")
    output=move(chel)
    print(output)
}
