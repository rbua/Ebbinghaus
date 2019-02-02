using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace TEST_magiword_dev_preview.ServiceHelper
{
    /// <summary>
    /// Сводное описание для LogIn
    /// </summary>
    public class SignIn : IHttpHandler
    {
        private string email;
        private string password;
        

        public void ProcessRequest(HttpContext context)
        {

            dynamic outputJSON = new JObject();
            context.Response.ContentType = "text/plain";

            email = context.Request.Form["email"];
            password = context.Request.Form["password"];

            if (string.IsNullOrEmpty(email) || string.IsNullOrEmpty(password))
                AuthentifcationError(outputJSON);
            if (User.Exist(email))
            {
                string truePass = User.GetPasswordByEmail(email);
                if (string.IsNullOrEmpty(password))
                    AuthentifcationError(outputJSON);
                if (password != truePass)
                    AuthentifcationError(outputJSON);
                else
                {
                    outputJSON.IsValid = "true";
                    outputJSON.Message = $"Signed as {email} - {password}";
                }   
            }
            else
                AuthentifcationError(outputJSON);


            context.Response.Write(outputJSON);
        }

        private void AuthentifcationError(dynamic j)
        {
            j.Successful = "false";
            j.ERROR = "Email or password doesn`t exist";
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