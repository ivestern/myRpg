import Game.*
import com.varabyte.kotter.foundation.*
import java.awt.Toolkit
import com.varabyte.kotter.foundation.collections.*
import com.varabyte.kotter.foundation.input.*
import com.varabyte.kotter.foundation.render.aside
import com.varabyte.kotter.foundation.render.offscreen
import com.varabyte.kotter.foundation.text.*
import com.varabyte.kotter.runtime.*
import com.varabyte.kotter.runtime.terminal.Terminal
import com.varabyte.kotter.terminal.system.SystemTerminal
import com.varabyte.kotter.terminal.virtual.TerminalSize
import com.varabyte.kotter.terminal.virtual.VirtualTerminal
import com.varabyte.kotterx.decorations.BorderCharacters
import com.varabyte.kotterx.decorations.bordered
import kotlinx.coroutines.delay
import java.awt.Dimension

val screenSize = Toolkit.getDefaultToolkit().screenSize
fun main()=session(clearTerminal = true, terminal =VirtualTerminal.create(title = "text RPG",fontSize = 20, terminalSize =TerminalSize(screenSize.width,screenSize.height))) {
    val chel = Player("Name")
    output=move(chel)
    print(output)
}
