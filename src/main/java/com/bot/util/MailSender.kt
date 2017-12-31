package com.bot.util

import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.util.Optional
import java.util.Properties

object MailSender {
	
	private val pass = Optional.ofNullable(System.getenv("mailpass")).orElseGet {
		Optional.ofNullable(System.getenv("mailpass")).orElseGet {
			BufferedReader(InputStreamReader(FileInputStream("C:/avantmail.dat"))).readLine()
		}
	}
	
	
	fun sendMail(subject: String, body: String, vararg to: String) {
		val props = System.getProperties()
		val host = "smtp.gmail.com"
		val from = "zapomni.org"
		props.put("mail.smtp.starttls.enable", "true")
		props.put("mail.smtp.host", host)
		props.put("mail.smtp.user", from)
		props.put("mail.smtp.password", pass)
		props.put("mail.smtp.port", "587")
		props.put("mail.smtp.auth", "true")
		
		val session = Session.getDefaultInstance(props)
		val message = MimeMessage(session)
		
		try {
			message.setFrom(InternetAddress(from))
			val toAddress = arrayOfNulls<InternetAddress>(to.size)
			
			for (i in to.indices) {
				toAddress[i] = InternetAddress(to[i])
			}
			
			for (address in toAddress) {
				message.addRecipient(Message.RecipientType.TO, address)
			}
			
			message.subject = subject
			message.setContent(body.replace("\n", "<br>"), "text/html; charset=utf-8")
			val transport = session.getTransport("smtp")
			transport.connect(host, from, pass)
			transport.sendMessage(message, message.allRecipients)
			transport.close()
		} catch (me: MessagingException) {
			me.printStackTrace()
		}
		
	}
	
}
