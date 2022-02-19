#!/usr/bin/env bash

# https://github.com/heroku/heroku-buildpack-jvm-common/blob/main/opt/jdbc.sh

set_jdbc_url() {
    local database_url=${1}
    local database_url_env_var=${2}
    local database_username_env_var=${3}
    local database_password_env_var=${4}

    local pattern="^([a-zA-Z][a-zA-Z0-9\+\.\-]*)://(.*?@)?([^/:]+)(:[0-9]+)?([^#\?]+)?(\?[^#]+)?(#.+)?$"

    if [[ ! $database_url =~ $pattern ]]; then
        # We dont consider a non matching string an error and silently exit
        echo "no matching database_url found"
        return 0
    else
        # NOTE: These variables also contain delimiters for easier re-concatentation later.
        # (i.e. :1234 instead of 1234 for port or user:pass@ instead of user:pass for user info.)
        local original_user_info="${BASH_REMATCH[2]}"
        local original_host="${BASH_REMATCH[3]}"
        local original_port="${BASH_REMATCH[4]}"
        local original_path="${BASH_REMATCH[5]}"
        local original_query="${BASH_REMATCH[6]}"
        local original_fragment="${BASH_REMATCH[7]}"

        # Split the original query string into an associative array. We use this array to keep track of all query
        # parameters for the final JDBC URL. It will be modified by later parts of this function.
        declare -A query_parameters
        local current_key=""
        for value in ${original_query//[?&=]/ }; do
            if [[ -z $current_key ]]; then
                current_key=$value
            else
                query_parameters[$current_key]=$value
                current_key=""
            fi
        done

        # Populate username and password variables for later use. We also add those to the array of query parameters.
        local username
        local password
        if [[ $original_user_info =~ ^(.+?):(.+?)@$ ]]; then
            username="${BASH_REMATCH[1]}"
            password="${BASH_REMATCH[2]}"
            query_parameters["user"]=$username
            query_parameters["password"]=$password
        elif [[ $original_user_info =~ ^(.+?)@$ ]]; then
            username="${BASH_REMATCH[1]}"
            query_parameters["user"]=$username
        fi

        # Database specific transformations based on the URL schema.
        local modified_schema
        modified_schema="jdbc:postgresql"
        if [[ "${CI:-}" != "true" ]]; then
            query_parameters["sslmode"]="require"
        fi

        # Fold all query parameters from the associative array into a query string.
        local modified_query

        local -r sorted_query_parameter_keys=$(echo -n "${!query_parameters[@]}" | tr " " "\n" | sort | tr "\n" " ")
        for query_parameter_key in $sorted_query_parameter_keys; do
            local key_value_pair="${query_parameter_key}=${query_parameters[$query_parameter_key]}"

            if [[ -z "${modified_query:-}" ]]; then
                modified_query="?${key_value_pair}"
            else
                modified_query="${modified_query}&${key_value_pair}"
            fi
        done

        local jdbcurl="${modified_schema}://${original_host}${original_port}${original_path}${modified_query}${original_fragment}"

        echo "setting: ${database_url_env_var}, ${database_username_env_var}, ${database_password_env_var}"
        # echo "values: ${jdbcurl} ${username} ${password}"
        eval "export ${database_username_env_var}=\"${username}\""
        eval "export ${database_password_env_var}=\"${password}\""
        eval "export ${database_url_env_var}=\"${jdbcurl}\""
    fi

}

set_jdbc_url "$DATABASE_URL" "DATASOURCE_JDBC_URL" "DATASOURCE_USERNAME" "DATASOURCE_PASSWORD"
