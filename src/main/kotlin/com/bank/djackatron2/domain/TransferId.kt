package com.bank.djackatron2.domain

/** Identity of an accepted transfer — a tracking handle returned to the caller (202 Accepted). */
@JvmInline
value class TransferId(val value: String)
