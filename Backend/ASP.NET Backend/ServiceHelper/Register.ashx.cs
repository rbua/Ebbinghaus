using Newtonsoft.Json.Linq;
using System;
using System.Configuration;
using System.Data.SqlClient;
using System.Web;

namespace TEST_magiword_dev_preview.ServiceHelper
{
    /// <summary>
    /// Сводное описание для Register
    /// </summary>
    public class Register : IHttpHandler
    {
        private string name;
        private string surname;
        private string password;
        private string email;
        private string preferedLanguage;

        private bool PrimaryValid(HttpContext context)
        {
            // need to regex validate email and password
            // Primary data execution and validation
            name = context.Request.Form["name"];
            surname = context.Request.Form["surname"];
            password = context.Request.Form["password"];
            email = context.Request.Form["email"];
            preferedLanguage = context.Request.Form["preferedLanguage"];
            if (name == null || surname == null || password == null || email == null || preferedLanguage == null)
            {

                return false;
            }
            return true;


        }
        private void RegisterUser(HttpContext context)
        {

            dynamic OutputJSON = new JObject();
            //
            if (!PrimaryValid(context))
            {
                OutputJSON.IsValid = "false";
                OutputJSON.ERROR = "USER MUST FILL ALL FIELDS";
                context.Response.Write(OutputJSON);
                return;
            }

            // User Validation
            if (!User.Exist(email))
            {
                OutputJSON.IsValid = "false";
                OutputJSON.ERROR = "Email is already registred. Please Sign in.";
                context.Response.Write(OutputJSON);
            }
            else { 
            //TODO: add logic when user was unregistred till now
                string connectionString = ConfigurationManager.ConnectionStrings["UserDBConnectionString"].ConnectionString;
                var _connection = new SqlConnection(connectionString);
                _connection.Open();

                bool possible;
                SqlCommand command2 = new SqlCommand($"INSERT INTO userdata VALUES (1, '{User.GetUserGuid(email, password, out possible)}')");
                command2.Connection = _connection;
                var writer2 = command2.ExecuteNonQuery();


                SqlCommand command = new SqlCommand( $"INSERT INTO userinfo VALUES ((select max(ID) from userdata), '{name}', '{surname}', '{password}', '{email}', '{preferedLanguage}')" );
                command.Connection = _connection;
                var writer = command.ExecuteNonQuery();
                _connection.Close();


                OutputJSON.IsValid = "true";
                OutputJSON.RowsAffected = writer;
                context.Response.Write(OutputJSON);

            }
        }

        public void ProcessRequest(HttpContext context)
        {
            
            context.Response.ContentType = "text/plain";
            RegisterUser(context);

        }

        public bool IsReusable
        {
            get
            {
                return false;
            }
        }
    }
}