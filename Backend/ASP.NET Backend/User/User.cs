using System;
using System.Configuration;
using System.Data.SqlClient;
using System.Text;

namespace TEST_magiword_dev_preview
{
    public class User
    {
        public static bool Exist(string email)
        {

            string connectionString = ConfigurationManager.ConnectionStrings["UserDBConnectionString"].ConnectionString;
            var _connection = new SqlConnection(connectionString);
            _connection.Open();

            SqlCommand command = new SqlCommand($"select count(ui.UserID) from userinfo ui where ui.Email = '{email}'");
            command.Connection = _connection;
            int result = Int32.Parse(command.ExecuteScalar().ToString());
            _connection.Close();
            if (result == 0)
                return false;
            return true;
        }

        public static string GetUserGuid(string password, string email, out bool exist)
        { 
            StringBuilder guid = new StringBuilder();
            guid.Append(password.GetHashCode());
            guid.Append(email.GetHashCode());
            guid.Append(GetUserID(email, out exist).GetHashCode());
            return guid.ToString();
        }

        internal static int GetUserID(string email, out bool isPosible)
        {
            isPosible = false;
            int id = int.MinValue;
            if (email == null | !Exist(email))
                return id;

            string connectionString = ConfigurationManager.ConnectionStrings["UserDBConnectionString"].ConnectionString;
            var _connection = new SqlConnection(connectionString);
            _connection.Open();

            SqlCommand command = new SqlCommand($"select ui.UserID from userinfo ui where ui.Email = '{email}'");
            command.Connection = _connection;
            _connection.Close();
            object result = command.ExecuteScalar();
            if (result == null)
                return Int32.MinValue;
            id = Int32.Parse(Convert.ToString( result ));
            if (id != Int32.MinValue)
                isPosible = true;
            return id;
        }

        internal static string GetPasswordByEmail(string email)
        {
            if (!Exist(email))
                return null;

            string connectionString = ConfigurationManager.ConnectionStrings["UserDBConnectionString"].ConnectionString;
            var _connection = new SqlConnection(connectionString);
            _connection.Open();

            SqlCommand command = new SqlCommand($"select password from userInfo where Email = '{email}'", _connection);
            string val = Convert.ToString(command.ExecuteScalar());
            _connection.Close();
            return val;
        }

        internal static void ActionToDB(string guid)
        {
            string connectionString = ConfigurationManager.ConnectionStrings["UserDBConnectionString"].ConnectionString;
            var _connection = new SqlConnection(connectionString);
            _connection.Open();
            
            SqlCommand command2 = new SqlCommand($"INSERT INTO actions VALUES((select ID from userdata where userdata.GUID = '{guid}'), 1, 1, 1)");
            command2.Connection = _connection;
            _connection.Close();
            command2.ExecuteNonQuery();
        }
    }
}