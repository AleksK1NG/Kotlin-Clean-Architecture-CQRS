package com.alexander.bryksin.kotlinspringcleanarchitecture.infrastructure.account.persistance.repository

internal const val OPTIMISTIC_UPDATE_QUERY = """UPDATE microservices.accounts a 
                |SET email = :email, phone = :phone, country = :country, city = :city, post_code= :post_code, bio = :bio,
                |image_url = :image_url, balance_amount = :balance_amount, balance_currency = :balance_currency, status = :status,
                |version = :version, updated_at = :updated_at, created_at = :created_at
                |WHERE a.id = :id and version = :prev_version"""

internal const val INSERT_ACCOUNT_QUERY = """INSERT INTO microservices.accounts
                | (id, email, phone, country, city, post_code, bio, image_url, balance_amount, balance_currency, status, version, created_at, updated_at) 
                | VALUES (:id, :email, :phone, :country, :city, :post_code, :bio, :image_url, :balance_amount, :balance_currency, :status, :version, :created_at, :updated_at)"""
