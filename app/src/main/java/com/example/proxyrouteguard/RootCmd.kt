package com.example.proxyrouteguard

import java.io.DataOutputStream

object RootCmd {
    data class Result(val exitCode: Int, val stdout: String, val stderr: String)

    fun exec(command: String): Result {
        val process = Runtime.getRuntime().exec("su")
        val dos = DataOutputStream(process.outputStream)
        dos.writeBytes("$command\n")
        dos.writeBytes("exit\n")
        dos.flush()

        val stdout = process.inputStream.bufferedReader().readText()
        val stderr = process.errorStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        process.destroy()
        return Result(exitCode, stdout.trim(), stderr.trim())
    }
}
