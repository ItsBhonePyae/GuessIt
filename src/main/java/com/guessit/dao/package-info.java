/**
 * Data access layer — one DAO per model (UserDao, MovieDao, ...).
 *
 * All JDBC/SQL lives here; controllers never talk to the database directly.
 * Connection settings come from guessit.properties (loaded by DBUtil).
 */
package com.guessit.dao;
